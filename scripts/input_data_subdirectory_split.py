# This script breaks the input-data into many different subdirectories evenly split

import os, os.path

BASE_DIR = "../inputdata/"
NUM_SUBDIRECTORIES = 50
FILE_NAME_BASE = "batch_"

file_list = os.listdir( BASE_DIR )
print(file_list)

# Create the directories
for x in range(0, NUM_SUBDIRECTORIES):
    os.mkdir(BASE_DIR + FILE_NAME_BASE + str(x) )

# Create the files
folder_number = 0
for file in file_list:
    renamed = BASE_DIR + FILE_NAME_BASE + str(folder_number) + "/" + file
    currName = BASE_DIR + file

    os.rename( currName, renamed )
    folder_number = ( (folder_number + 1) % NUM_SUBDIRECTORIES )
