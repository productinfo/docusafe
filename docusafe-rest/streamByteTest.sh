trap error ERR

function error () {
	print "  A N    E R R O R    O C C U R R E D"
	exit 1
}
function print () {
	{
	echo "$(date) ==================================================================================="
	echo "$(date) $1"
	echo "$(date)  "
	} | tee -a curl.log
}

file=../docusafe-rest.client/target/dsc
filetosave=target/largefile
rm -f $filetosave
i="0"
while (( i<2 ))
do
	cat $file >> $filetosave
	let i=i+1
done
size=$(ls -sk $filetosave | cut -f  2 -d " ")

# write stream and read bytes and stream ===================
# ==========================================================
print "$(date) write file stream oriented with size $size"
java -jar $file -wb $filetosave

print "$(date) read file stream oriented with size $size"
java -jar $file -rs $filetosave $file-as-stream2
diff $filetosave $file-as-stream2

print "$(date) read file byte oriented with size $size"
java -jar $file -rb $filetosave $file-as-bytes
diff $filetosave $file-as-bytes

print "STREAM TESTING SUCCESSFULL"
