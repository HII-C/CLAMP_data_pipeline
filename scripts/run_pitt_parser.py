import threading
import os
import subprocess
import sys
import atexit

BASE_DIR = "../"
INPUT_DIR = BASE_DIR + "inputdata/completed/"
OUTPUT_DIR = BASE_DIR + "outputdata/"
PARSER_JAR_DIR = BASE_DIR + "PittParser/out/artifacts/CLAMP_data_pipeline_jar/CLAMP_data_pipeline.jar"
LOG_DIR = BASE_DIR + "parselogs/"

# 1st command line argument = username; 2nd commandline argument = db password
if( len(sys.argv) != 3 ):
    sys.exit()

DB_USERNAME = sys.argv[1]
DB_PASS = sys.argv[2]

# Threading vars
MAX_THREAD_COUNT = 6

# danger variables!
thread_file = None
parsing_done = False

# Condense the logs on exit
def condenseLogs():
    subprocess.call(["python", "log_condenser.py"])

atexit.register( condenseLogs )

def run_parser_on_thread( cv ):
    global thread_file
    global parsing_done

    file_name = None

    with cv:
        while thread_file == None and not parsing_done:
            cv.wait()
        file_name = thread_file
        thread_file = None
        cv.notify_all()

    if( parsing_done ):
        return

    print(file_name + " is running")

    input_file = INPUT_DIR + file_name
    output_file = OUTPUT_DIR + file_name

    if( not os.path.isfile( input_file ) or not os.path.isfile( output_file ) ):
        print("File does not exist: " + file_name)
        return

    log_file = open( LOG_DIR + "log_" + file_name, "w+" )
    subprocess.call([ "java", "-jar", PARSER_JAR_DIR, DB_USERNAME, DB_PASS, OUTPUT_DIR + file_name, INPUT_DIR + file_name ], stdout=log_file)

    print(file_name + " has completed")

    run_parser_on_thread( cv )

def parse():
    global thread_file
    global parsing_done

    active_threads = []

    cv = threading.Condition()
    for _thread_index in range(0, MAX_THREAD_COUNT):
        active_threads.append( threading.Thread( target = run_parser_on_thread, args = ( cv, ) ) )

    for thread in active_threads:
        thread.start()

    output_file_list = os.listdir( OUTPUT_DIR )
    for output_file in output_file_list:
        if( not output_file.endswith(".txt") ):
            continue

        print( "Parsing file: " + output_file )

        with cv:
            while( thread_file != None ):
                cv.wait()

            thread_file = output_file
            cv.notify_all()

    print( "Parsing has completed" )
    parsing_done = True
    cv.notify_all()

def cleanLogs():
    subprocess.call(["rm", "-rf", "../parselogs"])
    subprocess.call(["mkdir", "../parselogs"])

def main():
    cleanLogs()
    parse()

main()