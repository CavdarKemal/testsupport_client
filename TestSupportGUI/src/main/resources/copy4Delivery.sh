#!/bin/bash

VERSION=$1

# Input Validation
if [ -z "$VERSION" ]; then
  echo "Error: Empty version provided"
  exit -1
fi


SOURCE_DIR="/home/versionuser/cte_delivery/TestSupport/TestSupportGUI-$VERSION-distribution.zip"
echo $SOURCE_DIR
PATH_TO_CTE_SOFTWARE_PC10005715="/mnt/PC10005715/CTE"
PATH_TO_CTE_SOFTWARE_PC10006610="/mnt/PC10006610/CTE"

#if [ ! -d "$SOURCE_DIR" ]; then
#  echo "Error: Source directory '$SOURCE_DIR' does not exist"
#  exit -1
#fi


# Create destination directory
#mkdir -p "$PATH_TO_CTE_SOFTWARE_PC10005715/TestSupportGUI-$VERSION"
#mkdir -p "$PATH_TO_CTE_SOFTWARE_PC10006610/TestSupportGUI-$VERSION"

# Copy files/directories
echo "$SOURCE_DIR" "$PATH_TO_CTE_SOFTWARE_PC10005715/"
cp "$SOURCE_DIR" "$PATH_TO_CTE_SOFTWARE_PC10005715/"
echo "Successfully copied files/directories to '$PATH_TO_CTE_SOFTWARE_PC10005715/TestSupportGUI-$VERSION'"

cp "$SOURCE_DIR" "$PATH_TO_CTE_SOFTWARE_PC10006610/"
echo "Successfully copied files/directories to '$PATH_TO_CTE_SOFTWARE_PC10006610/TestSupportGUI-$VERSION'"

