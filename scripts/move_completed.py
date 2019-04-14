import os

# Variables for keeping track of the file paths and number of potential subdirectories in the input folder
OUTPUT_PATH = "../outputdata/"
INPUT_PATH = "../inputdata/batch_"
COMPLETED_PATH = "../inputdata/completed/"
NUM_SUBDIRECTORIES = 50

# Dictionary to store the files which have already been processed
file_dict = {}

# Check whether or not the completed folder has already been created; if not, create it
if not os.path.isdir(COMPLETED_PATH):
  os.mkdir(COMPLETED_PATH)

# Iterate over all of the output files and store them in the dictionary; the .txt output files should have the same names as the .txt input files
for filename in os.listdir(OUTPUT_PATH):
  if filename.endswith(".txt"):
    file_dict[filename] = True

# Check input files against the dictionary; if the input files are in the dictionary, move them to the completed folder so we do not process them again
for x in range(0, NUM_SUBDIRECTORIES):
  input_path = INPUT_PATH + str(x)
  for filename in os.listdir(input_path):
    if file_dict[filename]:
      file_path = input_path + filename
      completed_file_path = COMPLETED_PATH + filename
      os.rename(file_path, completed_file_path)