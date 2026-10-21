"""Local SQLite data and Firebase Realtime Database access for MyPrice.

Firebase Authentication runs in the Android app. This module sends its short-lived
ID token with owner writes. No service-account credential belongs in an APK.
"""

import json
import re
import sqlite3
import threading
from datetime import date, datetime
from urllib.parse import quote
from urllib.request import Request, urlopen
from Storage import database_path

DATABASE_URL = "https://suprise1happyday-default-rtdb.asia-southeast1.firebasedatabase.app"


class _ThreadLocalSQLite:
    """Give each Android worker thread its own connection and cursor."""

    def __init__(self, path):
        self.path = path
        self.local = threading.local()

    def _current(self):
        if not hasattr(self.local, "connection"):
            self.local.connection = sqlite3.connect(self.path, timeout=20)
            self.local.cursor = self.local.connection.cursor()
        return self.local.connection

    def cursor(self):
        return self

    def execute(self, sql, parameters=()):
        self._current()
        self.local.cursor.execute(sql, parameters)
        return self

    def fetchone(self):
        self._current()
        return self.local.cursor.fetchone()

    def fetchall(self):
        self._current()
        return self.local.cursor.fetchall()

    def commit(self):
        self._current().commit()

    def close(self):
        if hasattr(self.local, "connection"):
            self.local.connection.close()
            del self.local.connection
            del self.local.cursor


connection = _ThreadLocalSQLite(database_path())
cursor = connection.cursor()


def _migrate_old_account_cache():
    # The former app stored an owner password in SQLite. Clear it and the
    # defunct Firebase store cache once when switching to the new project.
    version = cursor.execute("PRAGMA user_version").fetchone()[0]
    if version < 2:
        existing = {row[0] for row in cursor.execute(
            "SELECT name FROM sqlite_master WHERE type='table'").fetchall()}
        for table in ("Account", "Item", "Shopping", "Cart", "Promotion",
                      "Login", "StoreItem"):
            if table in existing:
                cursor.execute("DELETE FROM " + table)
        cursor.execute("PRAGMA user_version = 2")
        connection.commit()


_migrate_old_account_cache()


def _bridge():
    from java import jclass
    return jclass("pmujtmk.yeo.myprice.FirebaseBridge")


def _identity(expected_uid=None):
    bridge = _bridge()
    uid = str(bridge.getUid() or "")
    if not uid or (expected_uid is not None and uid != str(expected_uid)):
        raise PermissionError("Sign in with the correct store account")
    token = str(bridge.getIdToken() or "")
    if not token:
        raise PermissionError("Firebase sign-in has expired")
    return uid, token


def _path(*segments):
    values = [str(segment) for segment in segments]
    if not all(re.fullmatch(r"[A-Za-z0-9_-]+", value) for value in values):
        raise ValueError("Invalid database path")
    return "/".join(values)


def _request(method, path, payload=None, authenticated=False):
    url = DATABASE_URL + "/" + path + ".json"
    if authenticated:
        _, token = _identity()
        url += "?auth=" + quote(token, safe="")
    body = None if method in ("GET", "DELETE") else json.dumps(payload).encode("utf-8")
    request = Request(url, data=body, headers={"Content-Type": "application/json"}, method=method)
    with urlopen(request, timeout=20) as response:
        raw = response.read()
    return json.loads(raw.decode("utf-8")) if raw else None


def EncryptData(Type, Value):
    raise RuntimeError("Legacy password encryption is disabled; use Firebase Authentication")


def DecryptData(Type, Value):
    raise RuntimeError("Legacy password decryption is disabled; use Firebase Authentication")

def PromotionValidation(start, end):
    if start == "Tap to set" and end == "Tap to set":
        return False
    else:
        ds, ms, ys = start.split("/")
        de, me, ye = end.split("/")

        start_date = date(int(ys), int(ms), int(ds))
        end_date = date(int(ye), int(me), int(de))
        today = date.today()

        delta = today - start_date
        delta2 = end_date - today

        if delta.days >= 0 and delta2.days >= 0:
            return True
        else:
            return False

def UpdateLog(ID, Action, StoreName, Location, Email):
    # The old global log exposed account email addresses. No remote log is
    # created in the new database.
    pass

########################### Callable Function ##################################

def CheckTable():
    print("Check Table")
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    existing_table = cursor.fetchall()
    
    if ("Account",) not in existing_table:
        cursor.execute("""CREATE TABLE Account(Store_ID text, StoreName text, Location text)""")
        connection.commit()
        print("Account Table Created")

    if ("Item",) not in existing_table:
        cursor.execute("""CREATE TABLE Item(Barcode text, Name text, Price number, All_Discount number, All_Limit number, Member_Discount number, Member_Limit number, Start_Day text, End_Day text, Image text)""")
        connection.commit()
        print("Item Table Created")

    if ("Shopping",) not in existing_table:
        cursor.execute("""CREATE TABLE Shopping(StoreID text, StoreName text, Budget number, Membership number)""")
        connection.commit()
        print("Shopping Table Created")

    if ("Cart",) not in existing_table:
        cursor.execute("""CREATE TABLE Cart(Barcode text, Name text, Price number, All_Discount number, All_Limit number, Member_Discount number, Member_Limit number, Start_Day text, End_Day text, Image text, Quantity number, CartID text)""")
        connection.commit()
        print("Cart Table Created")

    if ("Login",) not in existing_table:
        cursor.execute("""CREATE TABLE Login(UniqueID text, ID text, StoreName text, Location text, Email text, Password text)""")
        connection.commit()
        print("Login Table Created")

    if ("StoreItem",) not in existing_table:
        cursor.execute("""CREATE TABLE StoreItem(UniqueID text, Barcode text, Name text, Price number, All_Discount number, All_Limit number, Member_Discount number, Member_Limit number, Start_Day text, End_Day text, Image text, Last_Update text)""")
        connection.commit()
        print("StoreItem Table Created")
        
    if ("Promotion",) not in existing_table:
        cursor.execute("""CREATE TABLE Promotion(Barcode text, Name text, Price number, All_Discount number, All_Limit number, Member_Discount number, Member_Limit number, Start_Day text, End_Day text, Image text, Percentage number)""")
        connection.commit()
        print("Promotion Table Created")

    if ("History_Details",) not in existing_table:
        cursor.execute("""CREATE TABLE History_Details(ID text, Name text, Price number, All_Discount number, Member_Discount number, Start text, End text, Quantity number, Image text)""")
        connection.commit()
        print("History_Details Table Created")

    if ("History",) not in existing_table:
        cursor.execute("""CREATE TABLE History(ID text, Storename text, Membership number, Budget text, TotalSpend text, TotalDiscount text, Date text)""")
        connection.commit()
        print("History Table Created")

def ResetTable():
    print("Reset Table")
    cursor.execute('DELETE FROM Account;',)
    cursor.execute('DELETE FROM Item;',)
    cursor.execute('DELETE FROM Shopping;',)
    cursor.execute('DELETE FROM Cart;',)
    cursor.execute('DELETE FROM Promotion;',)
    connection.commit()

def DownloadStoreDetail():
    AllData = []
    for uid, data in (_request("GET", _path("stores")) or {}).items():
        if not isinstance(data, dict):
            continue
        name, location = data.get("name"), data.get("location")
        if name is not None and location is not None:
            AllData.append([uid, name, location])
    return AllData
        

def SaveStoreDetail(ID, Name, Location):
    cursor.execute("INSERT INTO Account VALUES(:Store_ID, :StoreName, :Location)",
                {
                    'Store_ID': ID,
                    'StoreName': Name,
                    'Location': Location
                    })
    connection.commit()

def DownloadItemList(ID):
    AllData = []
    for i, Data in (_request("GET", _path("items", ID)) or {}).items():
        if not isinstance(Data, dict):
            continue
        SetData = [
            i,
            Data.get('A) Barcode'),
            Data.get('B) Name'),
            Data.get('C) Price'),
            Data.get('D) All Discount'),
            Data.get('E) All Limit'),
            Data.get('F) Member Discount'),
            Data.get('G) Member Limit'),
            Data.get('H) Start Day'),
            Data.get('I) End Day'),
            Data.get('J) Image'),
            Data.get('K) Last Update')
        ]
        if SetData[1] is not None:
            AllData.append(SetData)
    return AllData

def SaveItemList(Barcode, Name, Price, All_Discount, All_Limit, Member_Discount, Member_Limit, Start, End, Image):
    cursor.execute("INSERT INTO Item VALUES(:Barcode, :Name, :Price, :All_Discount, :All_Limit, :Member_Discount, :Member_Limit, :Start_Day, :End_Day, :Image)",
                {
                    'Barcode': Barcode,
                    'Name': Name,
                    'Price': float(Price),
                    'All_Discount': float(All_Discount),
                    'All_Limit': int(All_Limit),
                    'Member_Discount': float(Member_Discount),
                    'Member_Limit': int(Member_Limit),
                    'Start_Day': Start,
                    'End_Day': End,
                    'Image': Image
                    })
    connection.commit()
   
    if float(Price) > 0 and PromotionValidation(Start, End):
        Off = 0
        if float(All_Discount) != float(Price) and float(Member_Discount) != float(Price): # both available
            # find the cheaper
            if float(Member_Discount) <= float(All_Discount): #member cheaper
                Off = 100 - ((float(Member_Discount)/float(Price)) * 100)
            else:
                Off = 100 - ((float(All_Discount)/float(Price)) * 100)
        elif float(Member_Discount) != float(Price):
            Off = 100 - ((float(Member_Discount)/float(Price)) * 100)
        else:
            Off = 100 - ((float(All_Discount)/float(Price)) * 100)
        cursor.execute("INSERT INTO Promotion VALUES(:Barcode, :Name, :Price, :All_Discount, :All_Limit, :Member_Discount, :Member_Limit, :Start_Day, :End_Day, :Image, :Percentage)",
                {
                    'Barcode': Barcode,
                    'Name': Name,
                    'Price': float(Price),
                    'All_Discount': float(All_Discount),
                    'All_Limit': int(All_Limit),
                    'Member_Discount': float(Member_Discount),
                    'Member_Limit': int(Member_Limit),
                    'Start_Day': Start,
                    'End_Day': End,
                    'Image': Image,
                    'Percentage': Off
                    })
        connection.commit()

def SaveHistory(HistoryID, Storename, Membership, Budget, TotalSpend, TotalDiscount):
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)

    Date = Day + "/" + Month + "/" + Year
    
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        cursor.execute("INSERT INTO History_Details VALUES(:ID, :Name, :Price, :All_Discount, :Member_Discount, :Start, :End, :Quantity, :Image)",
                {
                    'ID': HistoryID,
                    'Name': i[1],
                    'Price': float(i[2]),
                    'All_Discount': float(i[3]),
                    'Member_Discount': float(i[5]),
                    'Start': i[7],
                    'End': i[8],
                    'Quantity': int(i[10]),
                    'Image': i[9]
                    })
        connection.commit()
        
    cursor.execute("INSERT INTO History VALUES(:ID, :Storename, :Membership, :Budget, :TotalSpend, :TotalDiscount, :Date)",
            {
                'ID': HistoryID,
                'Storename': Storename,
                'Membership': int(Membership),
                'Budget': Budget,
                'TotalSpend': TotalSpend,
                'TotalDiscount': TotalDiscount,
                'Date': Date
                })
    connection.commit()

def GetAllHistory():
    ID = []
    StoreName = []
    Membership = []
    Budget = []
    TotalSpend = []
    TotalDiscount = []
    Date = []
    
    cursor.execute("SELECT *, oid FROM History ORDER BY ID DESC")
    records = cursor.fetchall()
    for i in records:
        ID.append(i[0])
        StoreName.append(i[1])
        Membership.append(str(i[2]))
        Budget.append(i[3])
        TotalSpend.append(i[4])
        TotalDiscount.append(i[5])
        Date.append(i[6])

    return [ID, StoreName, Membership, Budget, TotalSpend, TotalDiscount, Date]

def GetHistory(HistoryID):
    cursor.execute("SELECT *, oid FROM History WHERE ID = ?", (HistoryID,))
    records = cursor.fetchall()

    return [records[0][1], str(records[0][2]), records[0][3], records[0][4], records[0][5], records[0][6]]

def GetHistoryDetails(HistoryID):
    ItemName = []
    Price = []
    All_Discount = []
    Member_Discount = []
    Start = []
    End = []
    Quantity = []
    Image = []
    
    cursor.execute("SELECT *, oid FROM History_Details WHERE ID = ?", (HistoryID,))
    records = cursor.fetchall()
    for i in records:
        ItemName.append(i[1])
        Price.append(str(i[2]))
        All_Discount.append(str(i[3]))
        Member_Discount.append(str(i[4]))
        Start.append(i[5])
        End.append(i[6])
        Quantity.append(str(i[7]))
        Image.append(i[8])

    return [ItemName, Price, All_Discount, Member_Discount, Start, End, Quantity, Image]

def ClearHistory():
    cursor.execute('DELETE FROM History_Details;',)
    cursor.execute('DELETE FROM History;',)
    connection.commit()

def RemoveHistory(HistoryID):
    cursor.execute("DELETE FROM History_Details WHERE ID = ?", (HistoryID,))
    cursor.execute("DELETE FROM History WHERE ID = ?", (HistoryID,))
    connection.commit()

###################### STORE ###################### STORE ###################### STORE ###################### STORE ###################### STORE ###################### STORE ######################
def CheckKey(key):
    # Legacy registration keys cannot safely be validated inside an APK.
    return ["invalid", ""]

def RemoveKey(ID):
    # Approval is now performed in Firebase Console.
    return None
    
def CheckRegisteredEmail(email):
    raise RuntimeError("Use Firebase Authentication to check email availability")

def CheckUniqueID(ID):
    return _request("GET", _path("stores", ID)) is not None


def CheckMerchantApproval(uid):
    _identity(uid)
    return _request("GET", _path("merchantApprovals", uid), authenticated=True) is True

def RegisterNewAccount(UniqueID, StoreName, Location, Email, Password):
    uid, _ = _identity(UniqueID)
    _request("PUT", _path("approvalRequests", uid), {
        "storeName": StoreName,
        "location": Location,
        "email": Email,
    }, authenticated=True)

def Login(Email, Password):
    try:
        uid, _ = _identity()
        if str(_bridge().getEmail()).casefold() != str(Email).casefold():
            return 1
        return 0 if CheckMerchantApproval(uid) else 1
    except PermissionError:
        return 1
    except Exception:
        return 3

def GetLoginInformation(Email):
    uid, _ = _identity()
    if not CheckMerchantApproval(uid):
        raise PermissionError("Store account is waiting for approval")
    current_email = str(_bridge().getEmail())
    if current_email.casefold() != str(Email).casefold():
        raise PermissionError("Store email does not match the signed-in account")
    profile = _request("GET", _path("merchantProfiles", uid), authenticated=True)
    from_request = not profile
    if from_request:
        profile = _request("GET", _path("approvalRequests", uid), authenticated=True) or {}
    name = profile.get("storeName" if from_request else "name")
    location = profile.get("location")
    if not name or not location:
        raise ValueError("Store profile is incomplete")
    if from_request:
        _request("PUT", _path("merchantProfiles", uid), {
            "name": name,
            "location": location,
            "email": current_email,
        }, authenticated=True)
    elif profile.get("email") != current_email:
        _request("PATCH", _path("merchantProfiles", uid), {
            "email": current_email,
        }, authenticated=True)
    _request("PUT", _path("stores", uid), {
        "name": name,
        "location": location,
    }, authenticated=True)
    if from_request:
        _request("DELETE", _path("approvalRequests", uid), authenticated=True)
    return [uid, uid, name, location, current_email, ""]

def SaveLoginInformation(UniqueID, ID, StoreName, Location, Email, Password):
    uid = str(_bridge().getUid() or "")
    if uid != str(UniqueID) or uid != str(ID):
        raise PermissionError("Store account does not match the signed-in user")
    if str(_bridge().getEmail()).casefold() != str(Email).casefold():
        raise PermissionError("Store email does not match the signed-in user")
    cursor.execute("DELETE FROM Login")
    cursor.execute("INSERT INTO Login VALUES(:UniqueID, :ID, :StoreName, :Location, :Email, :Password)",
        {
            'UniqueID': UniqueID,
            'ID': ID,
            'StoreName': StoreName,
            'Location': Location,
            'Email': Email,
            'Password': ""
        })
    connection.commit()

def GetLoggedIn_UniqueID():
    cursor.execute("SELECT *, oid FROM Login")
    records = cursor.fetchall()
    return records[0][0]

def GetLoggedIn_ID():
    try:
        cursor.execute("SELECT *, oid FROM Login")
        records = cursor.fetchall()
        if records[0][1] != None:
            return records[0][1]
        else:
            return "Unavailable"
    except:
        return "Unavailable"

def GetLoggedIn_StoreName():
    cursor.execute("SELECT *, oid FROM Login")
    records = cursor.fetchall()
    return records[0][2]

def GetLoggedIn_Location():
    cursor.execute("SELECT *, oid FROM Login")
    records = cursor.fetchall()
    return records[0][3]

def GetLoggedIn_Email():
    cursor.execute("SELECT *, oid FROM Login")
    records = cursor.fetchall()
    return records[0][4]

def GetLoggedIn_Password():
    # Passwords are managed by Firebase Authentication, never by SQLite.
    return ""

def ResetLoginTable():
    print("Clear Table")
    cursor.execute('DELETE FROM Login;',)
    connection.commit()

def ResetStoreItemTable():
    print("Clear Table")
    cursor.execute('DELETE FROM StoreItem;',)
    connection.commit()

def SaveStoreItemList(UniqueID, Barcode, Name, Price, All_Discount, All_Limit, Member_Discount, Member_Limit, Start, End, Image, LastUpdate):
    cursor.execute("INSERT INTO StoreItem VALUES(:UniqueID, :Barcode, :Name, :Price, :All_Discount, :All_Limit, :Member_Discount, :Member_Limit, :Start_Day, :End_Day, :Image, :Last_Update)",
                {
                    'UniqueID': UniqueID,
                    'Barcode': Barcode,
                    'Name': Name,
                    'Price': float(Price),
                    'All_Discount': float(All_Discount),
                    'All_Limit': int(All_Limit),
                    'Member_Discount': float(Member_Discount),
                    'Member_Limit': int(Member_Limit),
                    'Start_Day': Start,
                    'End_Day': End,
                    'Image': Image,
                    'Last_Update': LastUpdate
                    })
    connection.commit()

def GetStoreItemList():
    cursor.execute("SELECT *, oid FROM StoreItem ORDER BY Name")
    records = cursor.fetchall()
    UniqueID = []
    Barcode = []
    Name = []
    Start = []
    End = []
    Image = []
    LastUpdate = []

    for i in records:
        UniqueID.append(i[0])
        Barcode.append(i[1])
        Name.append(i[2])
        Start.append(i[8])
        End.append(i[9])
        Image.append(i[10])
        LastUpdate.append(i[11])

    return [UniqueID, Barcode, Name, Start, End, Image, LastUpdate]

def GetStoreItemList2():
    cursor.execute("SELECT *, oid FROM StoreItem ORDER BY Name")
    records = cursor.fetchall()
    Price = []
    All = []
    Member = []

    for i in records:
        Price.append(i[3])
        All.append(i[4])
        Member.append(i[6])

    return [Price, All, Member]

def GetStoreItemList3():
    cursor.execute("SELECT *, oid FROM StoreItem ORDER BY Name")
    records = cursor.fetchall()
    All_Limit = []
    Member_Limit = []

    for i in records:
        All_Limit.append(i[5])
        Member_Limit.append(i[7])

    return [All_Limit, Member_Limit]

def RegisterItem(StoreID, Barcode, Name, Price, All_Dis, All_Limit, Mem_Dis, Mem_Limit, Start, End, Image):
    _identity(StoreID)
    
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)
    
    LastUpdate = Day + "/" + Month + "/" + Year
    
    response = _request("POST", _path("items", StoreID), {'A) Barcode': Barcode,
              'B) Name': Name,
              'C) Price': Price,
              'D) All Discount': All_Dis,
              'E) All Limit': All_Limit,
              'F) Member Discount': Mem_Dis,
              'G) Member Limit': Mem_Limit,
              'H) Start Day': Start,
              'I) End Day': End,
              'J) Image': Image,
              'K) Last Update': LastUpdate
             }, authenticated=True)
    return response["name"]
        
def AddNewItem(UniqueID, Barcode, Name, Price, All_Discount, All_Limit, Member_Discount, Member_Limit, Start, End, Image):
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)
    LastUpdate = Day + "/" + Month + "/" + Year
    
    cursor.execute("INSERT INTO StoreItem VALUES(:UniqueID, :Barcode, :Name, :Price, :All_Discount, :All_Limit, :Member_Discount, :Member_Limit, :Start_Day, :End_Day, :Image, :Last_Update)",
                {
                    'UniqueID': UniqueID,
                    'Barcode': Barcode,
                    'Name': Name,
                    'Price': float(Price),
                    'All_Discount': float(All_Discount),
                    'All_Limit': int(All_Limit),
                    'Member_Discount': float(Member_Discount),
                    'Member_Limit': int(Member_Limit),
                    'Start_Day': Start,
                    'End_Day': End,
                    'Image': Image,
                    'Last_Update': LastUpdate
                    })
    connection.commit()

def CheckRegisteredBarcode(ID, Barcode):
    _identity(ID)
    items = _request("GET", _path("items", ID)) or {}
    return any(isinstance(data, dict) and data.get('A) Barcode') == Barcode
               for data in items.values())

def UpdateItemInformation(StoreID, ID, Barcode, Name, Price, All_Dis, All_Limit, Mem_Dis, Mem_Limit, Start, End, Image):
    _identity(StoreID)
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)
    LastUpdate = Day + "/" + Month + "/" + Year
    
    _request("PATCH", _path("items", StoreID, ID), {
        'A) Barcode': Barcode,
        'B) Name': Name,
        'C) Price': Price,
        'D) All Discount': All_Dis,
        'E) All Limit': All_Limit,
        'F) Member Discount': Mem_Dis,
        'G) Member Limit': Mem_Limit,
        'H) Start Day': Start,
        'I) End Day': End,
        'J) Image': Image,
        'K) Last Update': LastUpdate
    }, authenticated=True)

def UpdateItem(UniqueID, Barcode, Name, Price, All_Discount, All_Limit, Member_Discount, Member_Limit, Start, End, Image):
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)

    LastUpdate = Day + "/" + Month + "/" + Year
    
    cursor.execute("""UPDATE StoreItem SET
                UniqueID = :UniqueID,
                Barcode = :Barcode,
                Name = :Name,
                Price = :Price,
                All_Discount = :All_Discount,
                All_Limit = :All_Limit,
                Member_Discount = :Member_Discount,
                Member_Limit = :Member_Limit,
                Start_Day = :Start_Day,
                End_Day = :End_Day,
                Image = :Image,
                Last_Update = :Last_Update
                            
                WHERE UniqueID = :UniqueID""",{
                    'UniqueID': UniqueID,
                    'Barcode': Barcode,
                    'Name': Name,
                    'Price': Price,
                    'All_Discount': All_Discount,
                    'All_Limit': All_Limit,
                    'Member_Discount': Member_Discount,
                    'Member_Limit': Member_Limit,
                    'Start_Day': Start,
                    'End_Day': End,
                    'Image': Image,
                    'Last_Update': LastUpdate
                    })
    connection.commit()

def RemoveItem(StoreID, ID):
    _identity(StoreID)
    _request("DELETE", _path("items", StoreID, ID), authenticated=True)

def RemoveItem2(ID):
    cursor.execute("DELETE FROM StoreItem WHERE UniqueID = ?", (ID,))
    connection.commit()

def GetUniqueID(StoreID, Barcode):
    _identity(StoreID)
    for item_id, data in (_request("GET", _path("items", StoreID)) or {}).items():
        if isinstance(data, dict) and data.get('A) Barcode') == Barcode:
            return item_id
    return ""

def UpdateAccountInformation(UniqueID, ID, StoreName, Location, Email, Password):
    uid, _ = _identity(UniqueID)
    if uid != str(ID):
        raise PermissionError("Store ID does not match the signed-in user")
    if not CheckMerchantApproval(uid):
        raise PermissionError("Store account is waiting for approval")
    _request("PUT", _path("stores", uid), {
        "name": StoreName,
        "location": Location,
    }, authenticated=True)
    _request("PATCH", _path("merchantProfiles", uid), {
        "name": StoreName,
        "location": Location,
        "email": Email,
    }, authenticated=True)

def UpdateAccountInformation2(UniqueID, ID, StoreName, Location, Email, Password):
    cursor.execute("""UPDATE Login SET
                UniqueID = :UniqueID,
                ID = :ID,
                StoreName = :StoreName,
                Location = :Location,
                Email = :Email,
                Password = :Password
                            
                WHERE UniqueID = :UniqueID""",{
                    'UniqueID': UniqueID,
                    'ID': ID,
                    'StoreName': StoreName,
                    'Location': Location,
                    'Email': Email,
                    'Password': ""
                    })
    connection.commit()

def DeleteAccount(UniqueID, ID):
    uid, _ = _identity(UniqueID)
    if uid != str(ID):
        raise PermissionError("Store ID does not match the signed-in user")
    _request("DELETE", _path("items", uid), authenticated=True)
    _request("DELETE", _path("stores", uid), authenticated=True)
    _request("DELETE", _path("merchantProfiles", uid), authenticated=True)
    _request("DELETE", _path("approvalRequests", uid), authenticated=True)

# Require manual add registration key
def AddRedeemCode():
    raise RuntimeError("Registration keys are no longer used; approve stores in Firebase Console")
            
            
        
