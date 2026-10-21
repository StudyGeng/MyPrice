import os
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from os.path import dirname, join
import sqlite3
import random
from datetime import date, datetime
from cryptography.fernet import Fernet
from Storage import database_path

filename = os.environ.get("MYPRICE_FIREBASE_SERVICE_ACCOUNT", join(dirname(__file__), "Data/service-account.json"))

cred = credentials.Certificate(filename)
firebase_admin.initialize_app(cred, {"databaseURL": os.environ.get("MYPRICE_FIREBASE_DATABASE_URL", "")})

filename1 = database_path()
connection = sqlite3.connect(filename1)
cursor = connection.cursor()

key_1 = os.environ.get("MYPRICE_PASSWORD_FERNET_KEY", "")
key_2 = os.environ.get("MYPRICE_DATA_FERNET_KEY", "")

def EncryptData(Type, Value):
    if Type == "Password":
        key = key_1
    else:
        key = key_2
    crypter = Fernet(key)
    EncryptedContent = crypter.encrypt(str(Value).encode("ascii"))
    return str(EncryptedContent, "utf-8")

def DecryptData(Type, Value):
    if Type == "Password":
        key = key_1
    else:
        key = key_2
    crypter2 = Fernet(key.encode("ascii"))
    DecryptedContent = crypter2.decrypt(Value.encode("ascii"))
    return str(DecryptedContent, "utf-8")

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
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)
    current_date = Day + "/" + Month + "/" + Year
    current_time = now.strftime("%H:%M:%S")

    try:
        db.reference('/Log').push({
            'A) ID': ID,
            'B) Action': Action,
            'C) Date': current_date,
            'E) Time': current_time,
            'F) Store Name': StoreName,
            'G) Location': Location,
            'H) Email': Email
        })
    except Exception:
        # A log failure must not reverse a completed account operation.
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
    for Data in (db.reference("/Account").get() or {}).values():
        if Data is None:
            continue
        SetData = [
            Data.get('A) ID'),
            Data.get('B) Store Name'),
            Data.get('C) Location'),
        ]
        if SetData[1] is not None:
            AllData.append(SetData)
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
    for i, Data in (db.reference("/" + str(ID)).get() or {}).items():
        if Data is None:
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
    ID = []
    KeyList = []

    returnOutput = []
    ref = db.reference("/Registration_Key")
    for i in ref.get() or {}:
        Data = ref.child(i).get()
        ID.append(i)
        KeyList.append(Data.get('Key'))

    if key in KeyList:
        for i, j in enumerate(KeyList):
            if j == key:
                returnOutput = [j, ID[i]]
                break
    else:
        returnOutput = ["invalid", ""]

    return returnOutput

def RemoveKey(ID):
    ref = db.reference("/Registration_Key")
    ref.child(ID).delete()
    
def CheckRegisteredEmail(email):
    ref = db.reference("/Account")
    EmailData = []
    for i in ref.get() or {}:
        Data = ref.child(i).get()
        EmailData.append(Data.get('D) Email'))
            
    if email in EmailData:
        return True
    else:
        return False

def CheckUniqueID(ID):
    ref = db.reference("/Account")
    UniqueIDList = []
    for i in ref.get() or {}:
        Data = ref.child(i).get()
        UniqueIDList.append(Data.get('A) ID'))
            
    if ID in UniqueIDList:
        return True
    else:
        return False

def RegisterNewAccount(UniqueID, StoreName, Location, Email, Password):
    ref = db.reference('/Account')
    ref.push({
        'A) ID': UniqueID,
        'B) Store Name': StoreName,
        'C) Location': Location,
        'D) Email': Email,
        'E) Password': EncryptData("Password", Password)
                })

    table = "/" + str(UniqueID)
    try:
        db.reference(table).push({'Default': 'Default'})
        UpdateLog(UniqueID, "Register Account", StoreName, Location, Email)
    except Exception:
        # The account write above succeeded; a missing placeholder or log
        # must not make registration appear to have failed.
        pass

def Login(Email, Password):
    try:
        Accounts = db.reference("/Account").get() or {}
    except Exception:
        return 3

    # 0 = Login Success, 1 = Wrong Email, 2 = Wrong Password, 3 = Connection Error
    for Data in Accounts.values():
        if Data is not None and Data.get('D) Email') == Email:
            return 0 if Password == DecryptData("Password", Data.get('E) Password')) else 2
    return 1

def GetLoginInformation(Email):
    ref = db.reference("/Account")
    UniqueIDData = []
    IDData = []
    StoreNameData = []
    LocationData = []
    EmailData = []
    PasswordData = []
    for i in ref.get() or {}:
        Data = ref.child(i).get()
        UniqueIDData.append(i)
        IDData.append(Data.get('A) ID'))
        StoreNameData.append(Data.get('B) Store Name'))
        LocationData.append(Data.get('C) Location'))
        EmailData.append(Data.get('D) Email'))
        PasswordData.append(Data.get('E) Password'))

    i = 0
    for j in EmailData:
        if j == Email:
            return [UniqueIDData[i], IDData[i], StoreNameData[i], LocationData[i], Email, PasswordData[i]]
            break
        i += 1

def SaveLoginInformation(UniqueID, ID, StoreName, Location, Email, Password):
    cursor.execute("INSERT INTO Login VALUES(:UniqueID, :ID, :StoreName, :Location, :Email, :Password)",
        {
            'UniqueID': UniqueID,
            'ID': ID,
            'StoreName': StoreName,
            'Location': Location,
            'Email': Email,
            'Password': Password
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
    cursor.execute("SELECT *, oid FROM Login")
    records = cursor.fetchall()
    return records[0][5]

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
    Table = "/" + str(StoreID)
    ref = db.reference(Table)
    
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)
    
    LastUpdate = Day + "/" + Month + "/" + Year
    
    ref.push({'A) Barcode': Barcode,
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
             })

    UniqueID = []
    for i in ref.get():
        UniqueID.append(i)

    return UniqueID[-1]
        
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
    ref = db.reference("/" + str(ID))
    BarcodeData = []
    for i in ref.get() or {}:
        Data = ref.child(i).get()
        BarcodeData.append(Data.get('A) Barcode'))
        
    if Barcode in BarcodeData:
        return True
    else:
        return False

def UpdateItemInformation(StoreID, ID, Barcode, Name, Price, All_Dis, All_Limit, Mem_Dis, Mem_Limit, Start, End, Image):
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)
    LastUpdate = Day + "/" + Month + "/" + Year
    
    table = "/" + str(StoreID)
    ref = db.reference(table)
    ref.child(ID).update({
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
    })

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
    table = "/" + str(StoreID)
    ref = db.reference(table)
    ref.child(ID).delete()

def RemoveItem2(ID):
    cursor.execute("DELETE FROM StoreItem WHERE UniqueID = ?", (ID,))
    connection.commit()

def GetUniqueID(StoreID, Barcode):
    ref = db.reference("/" + str(StoreID))
    UniqueID = ""
    IDData = []
    BarcodeData = []
    for i in ref.get() or {}:
        Data = ref.child(i).get()
        IDData.append(i)
        BarcodeData.append(Data.get('A) Barcode'))

    i = 0
    for b in BarcodeData:
        if b == Barcode:
            UniqueID = IDData[i]
            break
        i += 1

    return UniqueID

def UpdateAccountInformation(UniqueID, ID, StoreName, Location, Email, Password):
    ref = db.reference("/Account")
    ref.child(UniqueID).update({
        'A) ID': ID,
        'B) Store Name': StoreName,
        'C) Location': Location,
        'D) Email': Email,
        'E) Password': EncryptData("Password", Password)
    })
    UpdateLog(ID, "Update Account Information", StoreName, Location, Email)

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
                    'Password': Password
                    })
    connection.commit()

def DeleteAccount(UniqueID, ID):
    account_key = str(UniqueID)
    store_id = str(ID)
    if not account_key or not store_id or "/" in account_key or "/" in store_id:
        raise ValueError("Invalid account identifier")
    db.reference().update({
        "Account/" + account_key: None,
        store_id: None,
    })
    UpdateLog(ID, "Delete Account", "None", "None", "None")

# Require manual add registration key
def AddRedeemCode():
    Characters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]

    RedeemCode = []
    for k in range(0, 32):
        while True:
            Redeem = ""
            for i in range(0, 4):
                for j in range(0, 4):
                    random.shuffle(Characters)
                    Redeem += Characters[0]
                if i != 3:
                    Redeem += "-"
            if Redeem not in RedeemCode:
                break
        RedeemCode.append(Redeem)

    for i in RedeemCode:
        print(i)
        ref = db.reference('/Registration_Key')
        ref.push({
            'Key': i
                })
            
            
        
