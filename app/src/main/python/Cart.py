import sqlite3
import random
from datetime import date
from Storage import database_path

filename = database_path()
connection = sqlite3.connect(filename)
cursor = connection.cursor()

def GetRMForm(value):
    value = float(value)
    RMform = "RM" + format(value, '.2f')
    return RMform

def PromotionValidation(start, end):
    if start != "Tap to set":
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
    else:
        return False

def GetTheLimit(price, all_discount, all_limit, member_discount, member_limit):
    if CheckMembership() == 1:
        if all_discount != price and member_discount != price: # both available
            if member_discount <= all_discount: #member cheaper
                return member_limit
            else: # all cheaper
                return all_limit
        elif member_discount != price: # member only
            return member_limit
        else: # all
            return all_limit
    else:
        if all_discount != price: #all
            return all_limit
        else: # member only
            return 0

def CheckForSecondID(Barcode):
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    CartIDList = []
    for i in records:
        if i[0] == Barcode:
          CartIDList.append(i[11])
        if len(CartIDList) == 2:
          break
    
    if len(CartIDList) == 2:
          return CartIDList
    else:
          return ["False", "False"]


def GetTotalNonRepeatedBarcode():
    BarcodeList = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        if i[0] not in BarcodeList:
            BarcodeList.append(i[0])
    return BarcodeList

def ResetCartQuantity(CartID1, CartID2):
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    CartID1_List = []
    CartID2_List = []
    for i in records:
        if i[11] == CartID1:
            CartID1_List = i
        elif i[11] == CartID2:
            CartID2_List = i
        if len(CartID1_List) != 0 and len(CartID2_List) != 0:
            break
        
    RemoveItemFromCart(CartID2)
    RemoveItemFromCart(CartID1)

    cursor.execute("INSERT INTO Cart VALUES(:Barcode, :Name, :Price, :All_Discount, :All_Limit, :Member_Discount, :Member_Limit, :Start_Day, :End_Day, :Image, :Quantity, :CartID)",
                {
            'Barcode': CartID1_List[0],
            'Name': CartID1_List[1],
            'Price': CartID1_List[2],
            'All_Discount': CartID1_List[3],
            'All_Limit': CartID1_List[4],
            'Member_Discount': CartID1_List[5],
            'Member_Limit': CartID1_List[6],
            'Start_Day': CartID1_List[7],
            'End_Day': CartID1_List[8],
            'Image': CartID1_List[9],
            'Quantity': CartID1_List[10] + CartID2_List[10],
            'CartID': CartID1
            })
    connection.commit()

def CreateSecondID(Barcode, Name, Price, Image, Quantity):
    ExistingCartID = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        ExistingCartID.append(i[11])

    # Infinity
    while True:
        CartID = str(random.randint(1, 10000))
        if CartID not in ExistingCartID:
            break
        
    cursor.execute("INSERT INTO Cart VALUES(:Barcode, :Name, :Price, :All_Discount, :All_Limit, :Member_Discount, :Member_Limit, :Start_Day, :End_Day, :Image, :Quantity, :CartID)",
                {
            'Barcode': Barcode,
            'Name': Name,
            'Price': Price,
            'All_Discount': Price,
            'All_Limit': 0,
            'Member_Discount': Price,
            'Member_Limit': 0,
            'Start_Day': "Tap to set",
            'End_Day': "Tap to set",
            'Image': Image,
            'Quantity': Quantity,
            'CartID': CartID
            })
    connection.commit()
    
########################### Callable Function ##################################

def AddToCart(Barcode):
    exist = False
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()

    for i in records:
        if i[0] == Barcode:
            exist = True
            break

    if exist:
        for i in records:
            if i[0] == Barcode:
                cursor.execute("""UPDATE Cart SET
                            Barcode = :Barcode,
                            Name = :Name,
                            Price = :Price,
                            All_Discount = :All_Discount,
                            All_Limit = :All_Limit,
                            Member_Discount = :Member_Discount,
                            Member_Limit = :Member_Limit,
                            Start_Day = :Start_Day,
                            End_Day = :End_Day,
                            Image = :Img,
                            Quantity = :Q,
                            CartID = :Cid
                            WHERE CartID = :Cid""",{
                                'Barcode': i[0],
                                'Name': i[1],
                                'Price': i[2],
                                'All_Discount': i[3],
                                'All_Limit': i[4],
                                'Member_Discount': i[5],
                                'Member_Limit': i[6],
                                'Start_Day': i[7],
                                'End_Day': i[8],
                                'Img': i[9],
                                'Q': i[10] + 1,
                                'Cid': i[11]
                                })
                connection.commit()
                break
    else:
        ExistingCartID = []
        for i in records:
            ExistingCartID.append(i[11])

        # Infinity
        while True:
            CartID = str(random.randint(1, 10000))
            if CartID not in ExistingCartID:
                break
            
        cursor.execute("SELECT *, oid FROM Item")
        records_2 = cursor.fetchall()
        for i in records_2:
            if i[0] == Barcode:
                cursor.execute("INSERT INTO Cart VALUES(:Barcode, :Name, :Price, :All_Discount, :All_Limit, :Member_Discount, :Member_Limit, :Start_Day, :End_Day, :Image, :Quantity, :CartID)",
                          {
                        'Barcode': i[0],
                        'Name': i[1],
                        'Price': i[2],
                        'All_Discount': i[3],
                        'All_Limit': i[4],
                        'Member_Discount': i[5],
                        'Member_Limit': i[6],
                        'Start_Day': i[7],
                        'End_Day': i[8],
                        'Image': i[9],
                        'Quantity': 1,
                        'CartID': CartID
                        })
                connection.commit()
                break   

def ResetAndManageCart():
    #Reset Cart
    for i in GetTotalNonRepeatedBarcode():
        SecondID = CheckForSecondID(i)
        FirstID = SecondID[0]
        SecondID = SecondID[1]
        if FirstID != "False":
            ResetCartQuantity(FirstID, SecondID)
    
    #Manage Cart
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        if PromotionValidation(i[7], i[8]):
            Limit = GetTheLimit(i[2], i[3], i[4], i[5], i[6])
            if i[10] > Limit and Limit != 0:
                # Update current data
                cursor.execute("""UPDATE Cart SET
                                Barcode = :Barcode,
                                Name = :Name,
                                Price = :Price,
                                All_Discount = :All_Discount,
                                All_Limit = :All_Limit,
                                Member_Discount = :Member_Discount,
                                Member_Limit = :Member_Limit,
                                Start_Day = :Start_Day,
                                End_Day = :End_Day,
                                Image = :Img,
                                Quantity = :Q,
                                CartID = :Cid
                                WHERE CartID = :Cid""",{
                                    'Barcode': i[0],
                                    'Name': i[1],
                                    'Price': i[2],
                                    'All_Discount': i[3],
                                    'All_Limit': i[4],
                                    'Member_Discount': i[5],
                                    'Member_Limit': i[6],
                                    'Start_Day': i[7],
                                    'End_Day': i[8],
                                    'Img': i[9],
                                    'Q': Limit,
                                    'Cid': i[11]
                                    })
                connection.commit()
                Quantity = i[10] - Limit;
                CreateSecondID(i[0], i[1], i[2], i[9], Quantity)

def GetBarcode():
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(i[0]))
    return List

def GetItemName():
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(i[1]))
    return List

def GetPrice():
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(GetRMForm(i[2])))
    return List

def GetAllDiscountPrice(): #
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(GetRMForm(i[3])))
    return List

def GetAllDiscountLimit(): #
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(i[4]))
    return List

def GetMemberDiscountPrice(): #
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(GetRMForm(i[5])))
    return List

def GetMemberDiscountLimit(): #
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(i[6]))
    return List

def GetStartPromotion(): #
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(i[7])
    return List

def GetEndPromotion(): #
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(i[8])
    return List

def GetImage():
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(i[9]))
    return List

def GetQuantity():
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(i[10]))
    return List

def GetCartID():
    List = []
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        List.append(str(i[11]))
    return List

def CheckBudget(RMform):
    cursor.execute("SELECT *, oid FROM Shopping")
    records = cursor.fetchall()
    budget = records[0][2]
    budget = GetRMForm(budget)

    if not RMform:
        temp = budget.strip("RM")
        budget = temp

    return budget

def CheckSpend(RMform): #######
    Membership = CheckMembership()
    
    spend = 0
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        if i[7] != "Tap to set" and PromotionValidation(i[7], i[8]):
            # Both offers available
            if float(i[3])!=i[2] and float(i[5])!=i[2]:
                # 1 >> Member, 0 >> Non-Member
                if Membership == 1:
                    # Cheaper is priority
                    # 3 = All, 5 = Member
                    if i[3] >= i[5]:
                        spend = spend + (i[5] * i[10])
                    else:
                        spend = spend + (i[3] * i[10])
                else:
                    spend = spend + (i[3] * i[10])
            # No Discount, only Member Price
            elif float(i[5])!=i[2] and Membership == 1:
                spend = spend + (i[5] * i[10])
            # No Member Price, only discount
            elif float(i[3])!=i[2]:
                spend = spend + (i[3] * i[10])
            # Both offers unavailable
            else:
                spend = spend + (i[2] * i[10])
        else:
            spend = spend + (i[2] * i[10])
            
    if RMform:
        spend = GetRMForm(spend)
    
    return spend

def CheckSavesAmount():
    spend = 0
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        spend = spend + (i[2] * i[10])
    pay = CheckSpend(False)
    saves = spend - pay
    saves = GetRMForm(saves)
    return saves

def CheckBalance(RMform):
    cursor.execute("SELECT *, oid FROM Shopping")
    records = cursor.fetchall()
    balance = records[0][2]

    spend = CheckSpend(False)
        
    balance = balance - spend
    
    if RMform:
        balance = GetRMForm(balance)
    
    return balance

def SpendLimitAlert():
    cursor.execute("SELECT *, oid FROM Shopping")
    records = cursor.fetchall()
    budget = records[0][2]
    LimitAlert = budget * 0.9

    if CheckSpend(False) >= LimitAlert:
        return True
    else:
        return False

def CheckMembership():
    cursor.execute("SELECT *, oid FROM Shopping")
    records = cursor.fetchall()
    return records[0][3]

def GetTotalItem():
    total = 0
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        total += i[10]
    return total

def UpdateQuantity(CartID, AddAmount): #################
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    
    for i in records:
        if i[11] == CartID:
            cursor.execute("""UPDATE Cart SET
                    Barcode = :Barcode,
                    Name = :Name,
                    Price = :Price,
                    All_Discount = :All_Dis,
                    All_Limit = :All_Limit,
                    Member_Discount = :Mem_Dis,
                    Member_Limit = :Mem_Limit,
                    Start_Day = :Start,
                    End_Day = :End,
                    Image = :Img,
                    Quantity = :Q,
                    CartID = :CartID
                                
                    WHERE CartID = :CartID""",{
                        'Barcode': i[0],
                        'Name': i[1],
                        'Price': i[2],
                        'All_Dis': i[3],
                        'All_Limit': i[4],
                        'Mem_Dis': i[5],
                        'Mem_Limit': i[6],
                        'Start': i[7],
                        'End': i[8],
                        'Img': i[9],
                        'Q': i[10] + int(AddAmount),
                        'CartID': i[11]
                        })
            connection.commit()
            break
                    
def RemoveItemFromCart(CartID):
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        if i[11] == CartID:
            FirstID = CheckForSecondID(i[0])[0]
            SecondID = CheckForSecondID(i[0])[1]
            if not PromotionValidation(i[7], i[8]) and FirstID != "False":
                #Second ID
                cursor.execute("DELETE FROM Cart WHERE CartID = ?", (CartID,))
            else:
                # First ID
                cursor.execute("DELETE FROM Cart WHERE Barcode = ?", (i[0],))
            break
    connection.commit()

def CheckPromotionLimit(CartID):
    valid = False
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        if i[11] == CartID and PromotionValidation(i[7], i[8]):
            Limit = GetTheLimit(i[2], i[3], i[4], i[5], i[6])
            if i[10] == Limit and Limit != 0:
                valid = True
    return valid

def CheckItemOverLimit(CartID):
    valid = False
    cursor.execute("SELECT *, oid FROM Cart")
    records = cursor.fetchall()
    for i in records:
        if i[11] == CartID and PromotionValidation(i[7], i[8]) and not CheckForSecondID(i[0])[0] == "False":
            valid = True

    return valid

