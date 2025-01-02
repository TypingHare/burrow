package burrow.carton.hoard

data class BackupFile(val fileName: String, val dateString: String) :
    Comparable<BackupFile> {
    override fun compareTo(other: BackupFile): Int {
        return fileName.compareTo(other.fileName)
    }
}