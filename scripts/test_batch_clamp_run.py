# After splitting input files into many sub-directories, run the shell script on each of those

import threading
import os
import subprocess
import multiprocessing

BASE_DIR = "../inputdata/"
SUBD_MIN = 0
SUBD_MAX = 4
FILE_NAME_BASE = "batch_"
OUTPUT_FOLDER = "../outputdata/batch_completed"

def run_clamp_on_thread( subdirectory_name ):
    subprocess.Popen([ "../run_pitt_pipeline.sh", subdirectory_name, "&" ]) 

def test_completion( processes ):
  checkIfComplete = True
  for process in processes:
    if process.poll() == None:
      checkIfComplete = False
      break
  if (checkIfComplete):
    print("Finished CLAMP!")
  else:
    print("Still processing the text")
    threading.Timer(30.0, test_completion( processes ))

def main():
    processes = []

    # loop through all the files
    for x in range(SUBD_MIN, SUBD_MAX):
        folder = BASE_DIR + FILE_NAME_BASE + str(x)
        processes.append(subprocess.Popen(["../run_pitt_pipeline.sh", folder, OUTPUT_FOLDER]))

    test_completion( processes )

main()
