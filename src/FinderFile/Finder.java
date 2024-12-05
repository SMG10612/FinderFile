package FinderFile;

public class Finder {
		// 컬럼정보에 따른 필드 선언
		private String FileName;
		private String ParentPath;
		private long FileSize;
		private String LastModifiedTime;
		private String NickName;
		
		// Getter/Setter 메서드
		public String getFileName() {
			return FileName;
		}
		public void setFileName(String FileName) {
			this.FileName = FileName;
		}
		
		public String getParentPath() {
			return ParentPath;
		}
		public void setParentPath(String ParentPath) {
			this.ParentPath = ParentPath;
		}
		
		public long getFileSize() {
			return FileSize;
		}
		public void setFileSize(long FileSize) {
			this.FileSize = FileSize;
		}
		
		public String getLastModifiedTime() {
			return LastModifiedTime;
		}
		public void setLastModifiedTime(String LastModifiedTime) {
			this.LastModifiedTime = LastModifiedTime;
		}
		
		public String getNickName() {
			return NickName;
		}
		public void setNickNamee(String NickName) {
			this.NickName = NickName;
		}
}
