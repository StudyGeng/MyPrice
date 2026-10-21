import re
import random
from email.message import EmailMessage
import ssl
import smtplib
from pdf_mail import sendpdf
from fpdf import FPDF
import qrcode
import PyPDF2
import os
from os.path import dirname, join
from datetime import date, datetime, time
from cryptography.fernet import Fernet
from uuid import uuid4

regex = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
Upper = ["A", "B", "C", "D", "E", "F", "G", "H", "I","J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"]
Lower = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o","p", "q", "r", "s", "t", "u", "v", "w", "y", "z"]
Number = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
AllowedSymbol = ["!", "@", "#", "_", "$", "%", "*"]

email_sender = os.environ.get("MYPRICE_EMAIL_SENDER", "")
email_password = os.environ.get("MYPRICE_EMAIL_PASSWORD", "")

key = os.environ.get("MYPRICE_QR_FERNET_KEY", "").encode("ascii")

def EncryptData(Value):
    crypter = Fernet(key)
    EncryptedContent = crypter.encrypt(str(Value).encode("ascii"))
    return str(EncryptedContent, "utf-8")

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
    crypter2 = Fernet(key.encode("ascii"))
    DecryptedContent = crypter2.decrypt(Value.encode("ascii"))
    return str(DecryptedContent, "utf-8")

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

    if len(key) != 19:
        validation[4] = False
        ErrorMessage += "1"
    else:
        ErrorMessage += "0"

    if False not in validation:
        return "Valid"
    else:
        return ErrorMessage

def StoreNameAndLocationValidation(StoreName, Location):
    ErrorMessage = ""
    validation = [True, True]
    if StoreName == "":
        ErrorMessage += "1"
        validation[0] = False
    elif StoreName[0] == " ":
        ErrorMessage += "1"
        validation[0] = False
    else:
        ErrorMessage += "0"

    if Location == "":
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
    username = ""
    for i in email_receiver:
        if i == "@":
            break
        username += i

    if request_type == "register":
        subject = "Single-use verification code"
        body = "Hi " + username + ",\n\nWe received your request for a verification code for the account registration. This is the verification details:\n\n"\
               + "Request Code: " + str(RequestCode) + "\nVerification Code: " + str(VerificationCode) + "\n\nIf you didn't request this code, you can safely ignore "\
               + "this email. Someone else might have typed your email address by mistake. \n\nThanks,\nMyPrice."

    elif request_type == "changeEmail":
        subject = "Single-use verification code"
        body = "Hi " + username + ",\n\nWe received your request for a verification code to verify a new email for your MyPrice's account. This is the verification details:\n\n"\
               + "Request Code: " + str(RequestCode) + "\nVerification Code: " + str(VerificationCode) + "\n\nIf you didn't request this code, you can safely ignore "\
               + "this email. Someone else might have typed your email address by mistake. \n\nThanks,\nMyPrice."
        
    elif request_type == "resetPassword":
        subject = "Reset Password Request"
        body = "Hi " + username + ".\n\n" + "We received a reset password request from you. This is your reset password verification details: \n\nRequest Code: "\
               + str(RequestCode) + "\nVerification Code: " + str(VerificationCode) + "\n\nPlease ignore this if you did not perform the password reset request. Someone else might "\
               + "have typed your email address by mistake. \n\nThanks,\nMyPrice."
    else:
        subject = "Delete Account Request"
        body = "Hi " + username + ".\n\n" + "We received a delete account request from you. This is your delete account verification details: \n\nRequest Code: "\
               + str(RequestCode) + "\nVerification Code: " + str(VerificationCode) + "\n\nPlease ignore this if you did not perform the delete account request. Someone else might "\
               + "trying to delete your account. Please make sure to update your password frequently.\n\nThanks,\nMyPrice."

    em = EmailMessage()
    em['From'] = email_sender
    em['To'] = email_receiver
    em['Subject'] = subject
    em.set_content(body)

    context = ssl.create_default_context()

    with smtplib.SMTP_SSL('smtp.gmail.com', 465, context=context) as smtp:
        smtp.login(email_sender, email_password)
        smtp.sendmail(email_sender, email_receiver, em.as_string())

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
    GenerateQRcode(ID)
    generatePDF(ID, StoreName, Location)
    
    username = ""
    for i in email_receiver:
        if i == "@":
              break
        username += i

    hidePassword = ""
    i = 0
    while i < len(password):
        if i < len(password) - 2:
            hidePassword += "*"
        else:
            hidePassword += password[i]
        i += 1

    if Type == "register":
        subject = "Registration completed!"
        body = "Hi, " + username + ".\n\nFirst of all, thanks a lot for registering your account. This is your registration detail: \n\nStore ID: " + ID + "\nStore Name: " + StoreName + "\nLocation: " \
                "" + Location + "\nEmail: " + email_receiver + "\nPassword: " + hidePassword + "\n\nThis email came with your store unique QR code. Don't " \
                "forget to print out and paste on the wall in your store. Together we create a better future!\n\nThanks,\nMyPrice."
    else:
        subject = "Your Unique QR Code"
        body = "Hi, " + username + ".\n\nThis is your store unique QR code and your store ID is " + ID + ". Don't forget to print out and paste on the wall in your store. Together we create a better future!\n\nThanks,\nMyPrice."
    
    path = OutputDirectory()

    send = sendpdf(email_sender, email_receiver, email_password, subject, body, "Your Unique QR Code", path)
    send.email_send()

def SendAccountChangedInformation(Type, StoreName, Location, Email, email_receiver):
    username = ""
    for i in email_receiver:
        if i == "@":
            break
        username += i

    subject = "Account information has been updated"

    if Type == "StoreInformation":
        body = "Hey " + username + "!\n\nWe would like to inform you that your store name has been changed to " + str(StoreName) + " and your store location has been changed to " + \
               str(Location) + ". Please click the button 'Generate Unique QR Code' in the Account page to get a new updated Unique QR Code for your store.\n\nIf you did not changed " + \
               "any store information recently, you are advised to change the password as soon as possible. Someone else might know your password then logged into your account and " + \
               "change the store information.\n\nThanks,\nMyPrice."
    elif Type == "Email":
        body = "Hey " + username + "!\n\nWe would like to inform you that your MyPrice account's email has been changed to " + str(Email) + ". This email (" + str(email_receiver) + ") will not longer " + \
               "used as your MyPrice account's email.\n\n If you did not changed your MyPrice account's email, you are advised to contact our tech support for the further actions. " + \
               "Someone else might know you password then logged into your account and change the email.\n\nThanks,\nMyPrice."
    else:
        body = "Hey " + username + "!\n\nWe would like to inform you that your MyPrice account's password has been changed.\n\nIf you did not changed your MyPrice account's password, " + \
               "you are advised to contact our tech support for the further actions. Someone else might know you password then logged into your account and change the email.\n\nThanks,\nMyPrice."

    em = EmailMessage()
    em['From'] = email_sender
    em['To'] = email_receiver
    em['Subject'] = subject
    em.set_content(body)

    context = ssl.create_default_context()

    with smtplib.SMTP_SSL('smtp.gmail.com', 465, context=context) as smtp:
        smtp.login(email_sender, email_password)
        smtp.sendmail(email_sender, email_receiver, em.as_string())

def SendDeletingAccountInformation(email_receiver):
    username = ""
    for i in email_receiver:
        if i == "@":
            break
        username += i

    subject = "Your account has been deleted"

    body = "Hi " + username + ".\n\nWe need to inform you that your account has been deleted permanently. Feel free to share your complains or suggested improvements to help us to improve" +\
           " our application. We always welcoming you back to be a part of MyPrice's store manager./n/nThanks,\nMyPrice."
    
    em = EmailMessage()
    em['From'] = email_sender
    em['To'] = email_receiver
    em['Subject'] = subject
    em.set_content(body)

    context = ssl.create_default_context()

    with smtplib.SMTP_SSL('smtp.gmail.com', 465, context=context) as smtp:
        smtp.login(email_sender, email_password)
        smtp.sendmail(email_sender, email_receiver, em.as_string())

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
    
