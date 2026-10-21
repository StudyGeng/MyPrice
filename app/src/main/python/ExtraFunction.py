import os
import re
import random
from email.message import EmailMessage
import ssl
import smtplib
from pdf_mail import sendpdf
from fpdf import FPDF
import qrcode
import PyPDF2
from os.path import dirname, join
import datetime

regex = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
Upper = ["A", "B", "C", "D", "E", "F", "G", "H", "I","J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"]
Lower = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o","p", "q", "r", "s", "t", "u", "v", "w", "y", "z"]
Number = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
AllowedSymbol = ["!", "@", "#", "_", "$", "%", "*"]

email_sender = os.environ.get("MYPRICE_EMAIL_SENDER", "")
email_password = os.environ.get("MYPRICE_EMAIL_PASSWORD", "")


def GenerateQRcode(ID):
    qr = qrcode.QRCode(version=2, box_size=50, border=5)
    qr.add_data(ID)
    qr.make(fit=True)
    img = qr.make_image(fill="black", back_color="white")
    path = join(dirname(__file__), "File/QRcode.png")
    img.save(path)

def generatePDF(ID, Name, Location):
    GenerateQRcode(ID)
    pdf = FPDF()
    pdf.add_page()

    QR_path = join(dirname(__file__), "File/QRcode.png")
    Template_Path = join(dirname(__file__), "File/TemplateQR.pdf")
    Output_Path = join(dirname(__file__), "File/Your Unique QR Code.pdf")
    watermark_file = join(dirname(__file__), "File/QRcode.pdf")

    pdf.set_text_color(255, 255, 255)

    pdf.set_font("Arial", size=50)
    pdf.cell(190, 60, txt="", align="C", ln=1)

    pdf.set_font("Arial", size=50)
    pdf.cell(190, 20, txt=Name, align="C", ln=2)

    pdf.set_font("Arial", size=25)
    pdf.cell(190, 15, txt=Location, align="C", ln=3)

    pdf.image(QR_path, w=160, h=160, x=25, y=112)
    pdf.output(watermark_file)

    with open(Template_Path, "rb") as filehandle_input:
        pdf = PyPDF2.PdfFileReader(filehandle_input)

        with open(watermark_file, "rb") as filehandle_watermark:
            watermark = PyPDF2.PdfFileReader(filehandle_watermark)
            first_page = pdf.getPage(0)
            first_page_watermark = watermark.getPage(0)
            first_page.mergePage(first_page_watermark)
            pdf_writer = PyPDF2.PdfFileWriter()
            pdf_writer.addPage(first_page)

            with open(Output_Path, "wb") as filehandle_output:
                pdf_writer.write(filehandle_output)
                

############################ Callable Functions ###########################

def GetDoubleForm(RMamount):
    amount = RMamount.strip("RM")
    return float(amount)

def GetRMForm(amount):
    value = float(amount)
    RMform = "RM" + format(value, '.2f')
    return RMform

def registrationValidation(store_name, location, email, password, confirm_password):
    validation = [True, True, True, True, True]
    ErrorMessage = "E"

    if store_name == "":
        ErrorMessage += "1"
        validation[0] = False
    elif store_name[0] == " ":
        ErrorMessage += "1"
        validation[0] = False
    else:
        ErrorMessage += "0"

    if location == "":
        ErrorMessage += "1"
        validation[1] = False
    elif location[0] == " ":
        ErrorMessage += "1"
        validation[1] = False
    else:
        ErrorMessage += "0"

    if not (re.fullmatch(regex, email)):
        ErrorMessage += "1"
        validation[2] = False
    else:
        ErrorMessage += "0"

    if len(password) < 8:
        ErrorMessage += "10"
        validation[3] = False

    else:
        password_validation = [False, False, False, False]

        for i in password:
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
            validation[3] = False
        else:
            ErrorMessage += "00"

    if confirm_password != password:
        ErrorMessage += "1"
        validation[4] = False
    else:
        ErrorMessage += "0"

    if False not in validation:
        return "Valid"
    else:
        return ErrorMessage

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
               + "this email. Someone else might have typed your email address by mistake. \n\nThanks,\nMyPrice"
    else:
        subject = "Reset Password Request"
        body = "Hey " + username + "!\n\n" + "We received a reset password request from you. This is your reset password verification details: \n\nRequest Code: "\
               + str(RequestCode) + "\nVerification Code: " + str(VerificationCode) + "\n\nPlease ignore this if you did not perform the password reset request. Someone else might "\
               + "have typed your email address by mistake. \n\nThanks,\nMyPrice"

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

def Send_QRcode_To_User(ID, StoreName, Location, email_receiver, password):
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

    subject = "Registration completed!"
    body = "Hi, " + username + ".\n\nFirst of all, thanks a lot for registering your account. This is your registration detail: \n\nStore name: " + StoreName + "\nLocation: " \
            "" + Location + "\nEmail: " + email_receiver + "\nPassword: " + hidePassword + "\n\nThis email came with your store unique QR code. Don't " \
            "forget to print out and paste on the wall in your store. Together we create a better future!\n\nThanks,\nMyPrice"
    
    path = join(dirname(__file__), "File")

    send = sendpdf(email_sender, email_receiver, email_password, subject, body, "Your Unique QR Code", path)
    send.email_send()

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
    LastUpdate = str(datetime.date.today())
    return LastUpdate

    
