import sqlite3
from Storage import database_path

filename = database_path()
connection = sqlite3.connect(filename)
cursor = connection.cursor()

def GetRMForm(value):
    value = float(value)
    RMform = "RM" + format(value, '.2f')
    return RMform

########################### Callable Function ###################################

def CheckQRCode(QR):
    cursor.execute("SELECT *, oid FROM Account")
    records = cursor.fetchall()
    
    exist = False
    StoreID = ""
    for i in records:
        if i[0] == str(QR):
            exist = True
            StoreID = i[0]
            break
    if exist:
        return StoreID
    else:
        return "Not Found"

def CheckBarcode(Barcode):
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    exist = False
    ItemID = ""
    for i in records:
        if i[0] == str(Barcode):
            exist = True
            ItemID = i[0]
            break
    if exist:
        return ItemID
    else:
        return "Not Found"

def GetItemName(Barcode):
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    ItemName = ""
    for i in records:
        if i[0] == str(Barcode):
            ItemName = i[1]
            break
    return ItemName

def GetItemPrice(Barcode):
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    ItemPrice = ""
    for i in records:
        if i[0] == str(Barcode):
            ItemPrice = i[2]
            break
    price = GetRMForm(ItemPrice)
    return price

def GetItemAllDiscountPrice(Barcode): #
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    ItemPrice = ""
    for i in records:
        if i[0] == str(Barcode):
            ItemPrice = i[3]
            break
    price = GetRMForm(ItemPrice)
    return price

def GetItemAllDiscountLimit(Barcode): #
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    Limit = ""
    for i in records:
        if i[0] == str(Barcode):
            Limit = i[4]
            break
    return Limit

def GetItemMemberDiscountPrice(Barcode): #
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    ItemPrice = ""
    for i in records:
        if i[0] == str(Barcode):
            ItemPrice = i[5]
            break
    price = GetRMForm(ItemPrice)
    return price

def GetItemMemberDiscountLimit(Barcode): #
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    Limit = ""
    for i in records:
        if i[0] == str(Barcode):
            Limit = i[6]
            break
    return Limit

def GetItemPromotionDuration(Barcode): #
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    Start = ""
    End = ""
    for i in records:
        if i[0] == str(Barcode):
            Start = i[7]
            End = i[8]
            break
    return (Start, End)

def GetItemImage(Barcode):
    cursor.execute("SELECT *, oid FROM Item")
    records = cursor.fetchall()
    Image = ""
    for i in records:
        if i[0] == str(Barcode):
            Image = i[9]
            break
    return str(Image)

def GetStoreName():
    cursor.execute("SELECT *, oid FROM Shopping")
    records = cursor.fetchall()
    return records[0][1]

def SetBudget(budget, membership):
    cursor.execute("SELECT *, oid FROM Shopping")
    records = cursor.fetchall()
    storeID = records[0][0]
    storeName = records[0][1]
    
    cursor.execute("""UPDATE Shopping SET
                    StoreID = :ID,
                    StoreName = :Name,
                    Budget = :B,
                    Membership = :Member
                    WHERE StoreID = :ID""",{
                        'ID': storeID,
                        'Name': storeName,
                        'B': float(budget),
                        'Member': int(membership)
                        })
    connection.commit()

def AddShoppingDetail(StoreID):
    cursor.execute("SELECT *, oid FROM Account")
    records = cursor.fetchall()
    for i in records:
        if i[0] == StoreID:
            StoreName = i[1]
            break
    cursor.execute("INSERT INTO Shopping VALUES(:StoreID, :StoreName, :Budget, :Membership)",
                {
                    'StoreID': StoreID,
                    'StoreName': StoreName,
                    'Budget': 0,
                    'Membership':0
                    })
    connection.commit()

def GetPromotionList():
    cursor.execute("SELECT *, oid FROM Promotion ORDER BY Percentage DESC")
    records = cursor.fetchall()
    
    Name = []
    Price = []
    AllDiscountPrice = []
    AllLimit = []
    MemberDiscountPrice = []
    MemberLimit = []
    Start = []
    End = []
    Image = []
    OffPercentage = []
    
    for i in records:
        Name.append(i[1])
        Price.append(str(i[2]))
        AllDiscountPrice.append(str(i[3]))
        AllLimit.append(str(i[4]))
        MemberDiscountPrice.append(str(i[5]))
        MemberLimit.append(str(i[6]))
        Start.append(i[7])
        End.append(i[8])
        Image.append(i[9])
        OffPercentage.append(str(i[10]))
        
    return [Name, Price, AllDiscountPrice, AllLimit, MemberDiscountPrice, MemberLimit, Start, End, Image, OffPercentage]

def CheckPromotion():
    cursor.execute("SELECT *, oid FROM Promotion")
    records = cursor.fetchall()
    if len(records) == 0:
        return False
    else:
        return True

