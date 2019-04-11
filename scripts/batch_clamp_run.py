# After splitting input files into many sub-directories, run the shell script on each of those

import threading
import os
import subprocess

BASE_DIR = "../inputdata/"
NUM_SUBDIRECTORIES = 50
FILE_NAME_BASE = "batch_"

def run_clamp_on_thread( subdirectory_name ):
    subprocess.call([ "../run_pitt_pipeline.sh", subdirectory_name ]) 

def main():
    threads = []

    # loop through all the files
    for x in range(0, NUM_SUBDIRECTORIES):
        folder = BASE_DIR + FILE_NAME_BASE + str(x)
        run_clamp_on_thread(folder)
        threads.append( threading.Thread( target = run_clamp_on_thread, args = (folder,) ) )

    for thread in threads:
        thread.start()

    print("Finished CLAMP!!")

main()
