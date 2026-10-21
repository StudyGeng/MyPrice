from os import environ
from os.path import dirname, getsize, isfile, join
from shutil import copyfile


def database_path():
    path = join(environ["HOME"], "MyPrice.db")
    previous_path = join(dirname(__file__), "Data", "MyPrice.db")
    if not isfile(path) and isfile(previous_path) and getsize(previous_path) > 0:
        copyfile(previous_path, path)
    return path
