import re
import random
from fpdf import FPDF
import qrcode
import PyPDF2
import os
from os.path import dirname, join
from datetime import date, datetime, time
from uuid import uuid4

regex = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
Upper = ["A", "B", "C", "D", "E", "F", "G", "H", "I","J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"]
Lower = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o","p", "q", "r", "s", "t", "u", "v", "w", "y", "z"]
Number = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
AllowedSymbol = ["!", "@", "#", "_", "$", "%", "*"]



def EncryptData(Value):
    return str(Value)

def OutputDirectory():
    path = join(os.environ["HOME"], "MyPriceOutput")
    os.makedirs(path, exist_ok=True)
    return path

def GenerateQRcode(ID):
    qr = qrcode.QRCode(version=2, box_size=50, border=5)
    qr.add_data(EncryptData(ID))
    qr.make(fit=True)
    img = qr.make_image(fill="black", back_color="white")
    path = join(OutputDirectory(), "QRcode.png")
    img.save(path)

def generatePDF(ID, Name, Location):
    GenerateQRcode(ID)
    pdf = FPDF()
    pdf.add_page()

    QR_path = join(OutputDirectory(), "QRcode.png")
    Template_Path = join(dirname(__file__), "File/TemplateQR.pdf")
    Output_Path = join(OutputDirectory(), "Your Unique QR Code.pdf")
    watermark_file = join(OutputDirectory(), "QRcode.pdf")

    pdf.set_text_color(255, 255, 255)

    pdf.set_font("Arial", size=50)
    pdf.cell(190, 75, txt="", align="C", ln=1)

    pdf.set_font("Arial", size=50)
    pdf.cell(190, 15, txt=Name, align="C", ln=2)

    pdf.set_font("Arial", size=25)
    pdf.cell(190, 15, txt=Location, align="C", ln=3)

    pdf.image(QR_path, w=140, h=140, x=35, y=125)
    pdf.output(watermark_file)

    with open(Template_Path, "rb") as filehandle_input:
        pdf = PyPDF2.PdfReader(filehandle_input)

        with open(watermark_file, "rb") as filehandle_watermark:
            watermark = PyPDF2.PdfReader(filehandle_watermark)
            first_page = pdf.pages[0]
            first_page_watermark = watermark.pages[0]
            first_page.merge_page(first_page_watermark)
            pdf_writer = PyPDF2.PdfWriter()
            pdf_writer.add_page(first_page)

            with open(Output_Path, "wb") as filehandle_output:
                pdf_writer.write(filehandle_output)
                

############################ Callable Functions ###########################
def DecryptData(Value):
    return str(Value)

def GetDoubleForm(RMamount):
    amount = RMamount.strip("RM")
    return float(amount)

def GetRMForm(amount):
    value = float(amount)
    RMform = "RM" + format(value, '.2f')
    return RMform

def registrationValidation(store_name, location, email, password, confirm_password, key):
    validation = [True, True, True, True, True]
    ErrorMessage = "E"

    if StoreNameAndLocationValidation(store_name, location) == "Valid":
        ErrorMessage += "00"
    else:
        validation[0] = False
        ErrorMessage += StoreNameAndLocationValidation(store_name, location)

    if EmailValidation(email) == "Valid":
        ErrorMessage += "0"
    else:
        validation[1] = False
        ErrorMessage += "1"
    
    if PasswordValidation(password) == "Valid":
        ErrorMessage += "00"
    else:
        validation[2] = False
        ErrorMessage += PasswordValidation(password)

    if ConfirmPasswordValidation(password, confirm_password) == "Valid":
        ErrorMessage += "0"
    else:
        validation[3] = False
        ErrorMessage += "1"

    # The old paid registration key database no longer exists. This field is an
    # optional reference for the manual store approval request.
    ErrorMessage += "0"

    if False not in validation:
        return "Valid"
    else:
        return ErrorMessage

def StoreNameAndLocationValidation(StoreName, Location):
    ErrorMessage = ""
    validation = [True, True]
    if StoreName == "" or len(StoreName) > 100:
        ErrorMessage += "1"
        validation[0] = False
    elif StoreName[0] == " ":
        ErrorMessage += "1"
        validation[0] = False
    else:
        ErrorMessage += "0"

    if Location == "" or len(Location) > 200:
        ErrorMessage += "1"
        validation[1] = False
    elif Location[0] == " ":
        ErrorMessage += "1"
        validation[1] = False
    else:
        ErrorMessage += "0"

    if False not in validation:
        return "Valid"
    else:
        return ErrorMessage

def EmailValidation(Email):
    if not (re.fullmatch(regex, Email)):
        return "1"
    else:
        return "Valid"

def PasswordValidation(Password):
    ErrorMessage = ""
    validation = [True, True]
    
    if len(Password) < 8:
        ErrorMessage += "10"
        validation[0] = False
    else:
        password_validation = [False, False, False, False]

        for i in Password:
            if i in Upper:
                password_validation[0] = True
            elif i in Lower:
                password_validation[1] = True
            elif i in Number:
                password_validation[2] = True
            elif i in AllowedSymbol:
                password_validation[3] = True

        if False in password_validation:
            ErrorMessage += "01"
            validation[1] = False
            
    if False not in validation:
        return "Valid"
    else:
        return ErrorMessage

def ConfirmPasswordValidation(Password, Confirm_Password):
    if Confirm_Password != Password:
        return "1"
    else:
        return "Valid"

def GenerateVerificationCode():
    i = 0
    Code = ""
    while i < 6:
        index = random.randint(0, 9)
        Code += Number[index]
        i += 1
    return Code

def GenerateRequestCode():
    i = 0
    Code = ""
    while i < 4:
       index = random.randint(0, 25)
       Code += Upper[index]
       i += 1
    return Code

def SendVerificationCode(email_receiver, VerificationCode, RequestCode, request_type):
    raise RuntimeError("Use Firebase Authentication email verification")

def GenerateUniqueID():
    char = Upper + Number
    i = 0
    ID = ""
    while i < 6:
        index = random.randint(0, 35)
        ID += char[index]
        i += 1
    return ID

def Send_QRcode_To_User(ID, StoreName, Location, email_receiver, password, Type):
    raise RuntimeError("Use the in-app store QR code")

def SendAccountChangedInformation(Type, StoreName, Location, Email, email_receiver):
    raise RuntimeError("Use Firebase Authentication notifications")

def SendDeletingAccountInformation(email_receiver):
    raise RuntimeError("Client-side email sending is disabled")

def ItemRegistrationValidation(barcode, name, price):
    validation = [True, True, True]
    ErrorMessage = "E"

    if barcode == "":
        ErrorMessage += "1"
        validation[0] = False
    elif barcode[0] == " ":
        ErrorMessage += "1"
        validation[0] = False
    else:
        ErrorMessage += "0"

    if name == "":
        ErrorMessage += "1"
        validation[1] = False
    elif name[0] == " ":
        ErrorMessage += "1"
        validation[1] = False
    else:
        ErrorMessage += "0"

    if price == "":
        ErrorMessage += "1"
        validation[2] = False
    else:
        ErrorMessage += "0"

    if False not in validation:
        return "Valid"
    else:
        return ErrorMessage

def GetLastUpdate():
    now = datetime.now()
    Day = str(now.today().day)
    Month = str(now.today().month)
    Year = str(now.today().year)

    LastUpdate = Day + "/" + Month + "/" + Year

    LastUpdate = Day + "/" + Month + "/" + Year
    return LastUpdate

def GetDateValue(date):
    list_date = date.split("/")
    return [int(list_date[0]), int(list_date[1]), int(list_date[2])]

def PromotionValidation(start, end):
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

def GenerateHistoryID():
    return datetime.now().strftime("%Y%m%d%H%M%S%f") + uuid4().hex[:8]
    
