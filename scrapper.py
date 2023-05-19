import os
import pandas as pd
import csv
import shutil

# Define constants
N = 20
FILENAME_TEMPLATE = "{}.txt"
FOLDER_PATH = "src/main/files"


# Specify the file path to the CSV
csv_file = 'songs.csv'

# Define the parameters for reading the CSV
kwargs = {
    'delimiter': ',',
    'quotechar': '"',
    'quoting': csv.QUOTE_ALL,
    'escapechar': '\\',
    'skipinitialspace': True,
    'encoding': 'latin-1'
}

# Load the CSV into a dataframe
txt_files_df = pd.read_csv(csv_file, **kwargs)

# Select first N rows
txt_files_df = txt_files_df.iloc[:N]

# Delete previous folder if it exists
if os.path.exists(FOLDER_PATH):
    shutil.rmtree(FOLDER_PATH)

# Create new folder
os.makedirs(FOLDER_PATH)

# Iterate over each row of the DataFrame and write to file
for index, row in txt_files_df.iterrows():
    filename = FILENAME_TEMPLATE.format(row.title)
    filepath = os.path.join(FOLDER_PATH, filename)
    with open(filepath, 'w', encoding='latin-1') as file:
        released = row.released.replace(" ", "")  # Remove spaces from the released column
        buffer = f"Title: {row.title}\nDescription: {row.description}\nAlbum: {row['appears on']}\nArtist(s): {row.artist}\nWriter(s): {row.writers}\nProducer(s): {row.producer}\nReleased: {released.split(',', 1)[-1]}"
        file.write(buffer)
