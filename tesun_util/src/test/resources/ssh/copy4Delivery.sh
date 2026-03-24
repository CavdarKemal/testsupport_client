#!/bin/bash

VERSION=$1

# Input Validation
if [ -z "$VERSION" ]; then
  echo "Error: Empty version provided"
  exit -1
fi

IFS='_'
read -ra newarr <<< "$VERSION"

if [ ${#newarr[@]} -ne 4 ]; then
  echo "Error: Version has not the correct format: w_x_y_z"
  exit -1
fi

for val in "${newarr[@]}"; do
  [[ $val =~ ^[0-9]+$ ]] || { echo "Error: Not a number: $val"; exit -1; } 
done

PATH_TO_CTE_SOFTWARE="/mnt/laufwerkT/Auslieferung/CrefoTEAM/Software/CrefoTeam_ExterneDaten"
SOURCE_DIR="/home/versionuser/cte_delivery/$VERSION-stableBranch-ausgeliefert/"

if [ ! -d "$SOURCE_DIR" ]; then
  echo "Error: Source directory '$SOURCE_DIR' does not exist"
  exit -1
fi

MAJOR="${newarr[0]}_${newarr[1]}"
MINOR="${newarr[0]}_${newarr[1]}_${newarr[2]}"
RELEASE="${newarr[3]}"

# Create destination directory
mkdir -p "$PATH_TO_CTE_SOFTWARE/$MAJOR/$MINOR/$RELEASE"

# Copy files/directories
cp -r "$SOURCE_DIR/"* "$PATH_TO_CTE_SOFTWARE/$MAJOR/$MINOR/$RELEASE/"

echo "Successfully copied files/directories to '$PATH_TO_CTE_SOFTWARE/$MAJOR/$MINOR/$RELEASE/'"

