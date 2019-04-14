import os

OUTPUT_PATH = "../outputdata/"
INPUT_PATH = "../inputdata/batch_"
COMPLETED_PATH = "../inputdata/completed/"
NUM_SUBDIRECTORIES = 50

file_dict = {}

if not os.path.isdir(COMPLETED_PATH):
  os.mkdir(COMPLETED_PATH)

for filename in os.listdir(OUTPUT_PATH):
  if filename.endswith(".txt"):
    file_dict[filename] = True

for x in range(0, NUM_SUBDIRECTORIES):
  input_path = INPUT_PATH + str(x)
  for filename in os.listdir(input_path):
    if file_dict[filename]:
      file_path = input_path + filename
      completed_file_path = COMPLETED_PATH + filename
      os.rename(file_path, completed_file_path)