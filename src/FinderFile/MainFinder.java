package FinderFile;

import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

class FindPanel extends JPanel implements ActionListener{
    private JLabel findLabel = new JLabel("파일명 입력(확장자 포함), 디렉토리 검색시 확장자 미포함");
    private JTextField findTextField; // 파일명을 입력받기 위한 JTextField
    private JTable resultTable;
    
    FinderDAO fdao = new FinderDAO();
    static Finder finder;
    

    public FindPanel() {
        setLayout(null); // 레이아웃 매니저를 사용하지 않음
        
        // 번호, 경로, 파일명, 크기, 수정시간의 값을 저장할 열
        String header[] = {"번호", "경로", "파일명", "크기", "수정 시간"};
        
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(header);
        
        resultTable = new JTable(tableModel);        
    
        // "경로" 열의 가로 길이 늘리기
        TableColumnModel columnModel = resultTable.getColumnModel();
        TableColumn column = columnModel.getColumn(1); // "경로" 열은 index 1
        column.setPreferredWidth(200); // 조절하고자 하는 가로 길이로 변경
        
        column = columnModel.getColumn(3); // "크기" 열은 index 3
        // 크기 열을 보이지 않게 한다
        column.setMinWidth(0);
        column.setMaxWidth(0);
        
        column = columnModel.getColumn(4); // "마지막 수정시간" 열은 index 4
        // 마지막 수정시간 열을 보이지 않게 한다
        column.setMinWidth(0);
        column.setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        
        scrollPane.setBounds(10, 10, 410, 200);
        add(scrollPane);

        findLabel.setBounds(10, 220, 350, 20);   

        findTextField = new JTextField(20);         // 파일명을 입력하는 필드
        findTextField.setBounds(10, 240, 410, 30);   // 필드의 위치 조정

        JButton findButton = new JButton("검색");
        findButton.setBounds(100, 280, 100, 30);
        
        findButton.addActionListener(this);

        JButton saveButton = new JButton("저장");
        saveButton.setBounds(220, 280, 100, 30);
        
        saveButton.addActionListener(this);

        add(findLabel);
        add(findTextField);
        add(findButton);
        add(saveButton);   
    }    
    // 파일 사이즈 컨버터
    private static String convertFileSize(long fileSizeInBytes) {
        if (fileSizeInBytes < 1024) {
            return fileSizeInBytes + " b";
        } else if (fileSizeInBytes < 1024 * 1024) {
            double fileSizeInKB = fileSizeInBytes / 1024.0;
            return String.format("%.2f kb", fileSizeInKB);
        } else if (fileSizeInBytes < 1024 * 1024 * 1024) {
            double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);
            return String.format("%.2f Mb", fileSizeInMB);
        } else {
            double fileSizeInGB = fileSizeInBytes / (1024.0 * 1024.0 * 1024.0);
            return String.format("%.2f Gb", fileSizeInGB);
        }
    }

    // FileTime을 yyyy-MM-dd a hh:mm 으로 포멧팅 하는 메서드
    private static String formatFileTime(FileTime fileTime) {
        // FileTime을 Date 객체로 변환
       // FileTime은 파일 시간 정보를 표현하는데 사용되는 클래스
       // toMillis() 메서드는 해당 FileTime을 밀리초 단위의 long 값으로 변환
        Date date = new Date(fileTime.toMillis());

        // SimpleDateFormat을 사용하여 원하는 형식으로 포맷팅 (년-월-일 오전/오후 시:분)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
        // dateFormat을 사용하여 Date 객체를 문자열로 포맷팅
        return dateFormat.format(date);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
       
       String fileFullName = findTextField.getText();   // textfield에 작성한 파일 및 디렉터리 이름
       String fileName = ""; // 파일 이름
       String fileExtension = "";   // 확장자명
       // 자바에서는 람다 표현식이나 익명 내부 클래스에서 사용되는 외부 지역 변수가 수정되지 않아야 하기 때문
       // 그러나 배열의 요소는 수정 가능한 객체이기 때문에
       boolean[] found = { false }; // 검색 결과 (탐색이 끝나고 찾은 파일이나 디렉터리가 있는지 없는지

       // StringTokenizer 를 이용해 파일명과 확장자를 분리한다
       StringTokenizer fileNameToken = new StringTokenizer(findTextField.getText(), ".");
       
       while (fileNameToken.hasMoreTokens()) {
           fileName = fileNameToken.nextToken();  // 다음 토큰을 기준으로 문자열을 반환

           // 토큰이 더 있으면 확장자로 간주
           if (fileNameToken.hasMoreTokens()) {
               fileExtension = fileNameToken.nextToken();
           } else {
               // 토큰이 더 없으면 확장자 없음
               fileExtension = "";
           }
       }
        //StringTokenizer는 토큰을 하나씩 순회하며 가져오는 방식으로
      //루프 내에서 계속해서 fileName과 fileExtension 값을 변경하기 때문에 오류가 발생
        String fn = fileName;
        String fe = fileExtension;
        
        if (fileFullName.isEmpty()) {   // 텍스트 필드에 작성하지 않고 버튼을 클릭하였을경우 경고창 출력
            JOptionPane.showMessageDialog(this, "검색어를 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
       if(e.getActionCommand() == "검색" ) {   // 검색 버튼을 누를 경우
          try {
             // DefaultTableModel의 메서드를 사용하기 위해 resultTable을 DefaultTableModel형으로 형 변환 하여 사용 한다
             // DefaultTableModel은 테이블의 데이터를 관리하기 위한 메서드를 제공
             DefaultTableModel tableModel = (DefaultTableModel)resultTable.getModel();
                tableModel.setRowCount(0); // 기존 데이터 지우기
                
             for (char drive = 'C'; drive <= 'Z'; drive++) { // C드라이브 부터 Z 드라이브 까지 순회
                // Path(파일이나 디렉터리의 경로를 나타낸다)
                // Paths.get() : String 또는 URI를 Path로 변환한다
                Path startPath = Paths.get(drive + ":\\");    // 처음에는 startPath에 C:\\ 가 저장된다
                if (Files.exists(startPath)) {   // 해당 드라이브가 존제할 경우 실행
                   Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                          
                           // visitFile : 각 파일을 방문할 때 호출
                           // BasicFileAttributes 객체 내부에 파일의 기본 속성을 가져올수 있는 메서드 존제
                           // ex)  attrs.lastModifiedTime() : 수정된 시간 반환
                      @Override
                           public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                               // 파일명과 확장자가 일치하는 경우 경로를 콘솔에 출력
                              // file.getFileName().toString().startsWith(fn) : 현재 방문 중인 파일의 이름이 사용자가 입력한 파일명(fn)으로 시작하는지
                              // startsWith(fn) : 문자열이 fn으로 시작하는지에 대한 여부 반환
                              if (fe != "" && file.getFileName().toString().startsWith(fn) && file.getFileName().toString().endsWith(fe)) {
                                 // new Object[]{ : 아래 값을 요소로 가지는 객체 배열을 생성
                                 // tableModel.getRowCount() + 1 : 현재 테이블의 행 수에 +1한 값을 반환 (새로운 행이 추가될 때 번호가 자동으로 증가됨)
                                 // file.getParent() : 현제 Path 객체의 상위 디렉토리를 나타내는 Path를 반환
                                 // attrs.lastModifiedTime() : 파일의 마지막 수정시간 반환
                                 // attrs.size() : 파일의 크기 반환 (byte단위)
                                 tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, file.getParent(), file.getFileName(), 
                                       attrs.size(), formatFileTime(attrs.lastModifiedTime())});
                                 // 디버깅용
                                 System.out.println("파일 : " + file.toString() + " (수정한 시간: " + attrs.lastModifiedTime() + ") (크기 : " 
                                        + attrs.size() + ")");
                                   found[0] = true; // 파일을 탐색하였으면 true
                               }
                              return FileVisitResult.CONTINUE;
                           }
                      // preVisitDirectory : 디렉토리에 들어가기 전에 호출
                      @Override
                           public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                              if (dir.getFileName() != null && dir.getFileName().toString().equals(fn) && fe.equals("")) {
                                 // 위와 동일
                                 tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, dir.getParent(), dir.getFileName(),
                                       attrs.size(), formatFileTime(attrs.lastModifiedTime())});
                                 
                                 // 디버깅용
                                 System.out.println("디렉터리 : " + dir.toString() + " (수정한 시간: " + attrs.lastModifiedTime() + ") (크기 : " 
                                         + attrs.size() + ")");
                                   found[0] = true; // 디렉터리를 탐색하였으면 true
                              }
                              return FileVisitResult.CONTINUE;
                           }
                      // visitFileFailed : 파일에 대한 방문이 실패했을 때 호출
                      @Override
                           public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                               // 파일에 접근할 수 없는 경우에 대한 처리
                               return FileVisitResult.CONTINUE;
                           }
                       });
                }
             } 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
          // 검색 결과가 없으면 메시지 표시
            if (!found[0]) {
                JOptionPane.showMessageDialog(this, "검색 결과가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            }
            // 검색이 끝났을 경우
            else if (found[0]) {
                JOptionPane.showMessageDialog(this, "검색이 완료되었습니다", "알림", JOptionPane.INFORMATION_MESSAGE);
            }
       }
       else if(e.getActionCommand() == "저장") {   // 저장을 클릭할 겅우
          // 선택된 행의 데이터 출력
          // resultTable.getSelectedRow() : resultTable에서 현재 선택된 행의 인덱스를 가져온다
          // 선택된 행이 없으면 -1을 반환
          int selectedRowIndex = resultTable.getSelectedRow();
          
          if (selectedRowIndex != -1) { // 선택된 행이 있다면
              // DefaultTableModel의 메서드를 사용하기 위해 resultTable을 DefaultTableModel형으로 형 변환 하여 사용
              DefaultTableModel tableModel = (DefaultTableModel) resultTable.getModel();
              FindPanel.finder = new Finder();
              FindPanel.finder.setParentPath(tableModel.getValueAt(selectedRowIndex, 1).toString());
              FindPanel.finder.setFileName(tableModel.getValueAt(selectedRowIndex, 2).toString());
              FindPanel.finder.setFileSize((long) tableModel.getValueAt(selectedRowIndex, 3));
              FindPanel.finder.setLastModifiedTime(tableModel.getValueAt(selectedRowIndex, 4).toString());
              System.out.println(FindPanel.finder.getFileName() + " " + FindPanel.finder.getParentPath());
              fdao.insertPath(FindPanel.finder);
          } else {
              // 선택된 행이 없을 때
             // JOptionPane.showMessageDialog() : 경고창 출력.
             // this : 부모 컴포넌트, 창 또는 프레임
             // JOptionPane.WARNING_MESSAGE : 경고 아이콘!!
             JOptionPane.showMessageDialog(this, "행을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
              System.out.println("선택한 행이 없습니다.");
          }
          // SavePanel 클래스의 인스턴스를 부모 컨테이너에서 찾아온다
          // getParent(): 현재 컴포넌트의 부모 컨테이너를 반환
          // getComponent(1): 부모 컨테이너에서 두 번째(인덱스 1) 자식 컴포넌트를 반환 즉 SavePanel 객체를 반환 ,(인덱스 0은 FindPanel)
          SavePanel savePanel = (SavePanel) getParent().getComponent(1);
          
          // FinderDAO 클래스의 updateTable 메서드를 호출하여 테이블 데이터를 업데이트 해줌으로 써
          // 데이터 베이스에 값 저장이 끝난 후 savePanel의 테이블을 업데이트 해줌으로 써 SavePanel 탭으로 넘어갔을 때에
          // resultTable에 데이터 베이스의 값을 불러와 넣을 수 있게 됨
          savePanel.fdao.UpdateTable(savePanel.tableModel);
       }   
    }
}
class SavePanel extends JPanel implements ActionListener{
    private JLabel findLabel = new JLabel("원하는 셀 클릭후 삭제 및 셀 더블클릭시 해당 파일 열기");
    private JTable resultTable;
    
    public FinderDAO fdao = new FinderDAO();
    public DefaultTableModel tableModel = new DefaultTableModel();
    //isMenu가 0일때 이름순으로 정렬 
    //isMenu가 1일때 날짜순으로 정렬
    //isMenu가 2일때 크기순으로 정렬
    private int isMenu = 0;
    
    private JMenuBar jmb = new JMenuBar(); //JMenuBar()객체 생성
    private JMenu menu = new JMenu("정렬");	// JMenu 객체 생성
    private JCheckBoxMenuItem jmciDesc = new JCheckBoxMenuItem("내림차순");  // "내림차순" 메뉴 추가
    private JCheckBoxMenuItem jmciAsc = new JCheckBoxMenuItem("오름차순"); // "오름차순" 메뉴 추가
    private JMenuItem jmiNameSort = new JMenuItem("이름 순"); // "이름 순" 메뉴 추가
    private JMenuItem jmiDateSort = new JMenuItem("날짜 순"); // "날짜 순" 메뉴 추가
    private JMenuItem jmiDataSort = new JMenuItem("크기 순"); // "크기 순" 메뉴 추가
   
    
    public SavePanel() {       
        setLayout(null); // 레이아웃 매니저를 사용하지 않음
       
        
        menu.add(jmiNameSort);
        menu.add(jmiDateSort);
        menu.add(jmiDataSort);
        menu.add(jmciDesc);
        menu.add(jmciAsc);
        
        ButtonGroup gb = new ButtonGroup();
        gb.add(jmciDesc);
        gb.add(jmciAsc);
        
        //내림차순 기본 선택
        gb.setSelected(jmciDesc.getModel(), true);  
        
        
        
        // "내림차순" 선택 시 호출되는 메서드 추가
        jmciDesc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               handles();
            }
        });
        
        // "오름차순" 선택 시 호출되는 메서드 추가
        jmciAsc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               handles();
            }
        });
        
        
        // "이름 순" 선택 시 호출되는 메서드 추가
        jmiNameSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               isMenu = 0;
               handles();
            }
        });
        
        // "날짜 순" 선택 시 호출되는 메서드 추가
        jmiDateSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               isMenu = 1;
               handles();
            }
        });
       
        // "크기 순" 선택 시 호출되는 메서드 추가
        jmiDataSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               isMenu = 2;
               handles();
            }
        });
        
        // JMenuBar를 JPanel에 추가
        jmb.add(menu);
        // JPanel에 JMenuBar 추가
        jmb.setBounds(10, 0, 35, 20);
        add(jmb);

        
        String header[] = {"별명", "경로", "파일명", "크기", "수정 시간"};
        
        DefaultTableModel tableModel = new DefaultTableModel(){
           // 테이블 셀 수정 불가능하게 설정
           @Override
            public boolean isCellEditable(int row, int column) {
                
                return false;
            }
        };
        
        // 테이블의 열에 header의 내용을 넣기
        tableModel.setColumnIdentifiers(header);
        
        resultTable = new JTable(tableModel);       
        
        // "경로" 열의 가로 길이 늘리기
        TableColumnModel columnModel = resultTable.getColumnModel();
        TableColumn column = columnModel.getColumn(1); // "경로" 열은 index 1
        column.setPreferredWidth(200); // 조절하고자 하는 가로 길이로 변경
        
        column = columnModel.getColumn(3); // "크기" 열은 index 3
        // 크기 열을 보이지 않게 한다
        column.setMinWidth(0);
        column.setMaxWidth(0);
        
        column = columnModel.getColumn(4); // "마지막 수정시간" 열은 index 4
        // 마지막 수정시간 열을 보이지 않게 한다
        column.setMinWidth(0);
        column.setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        
        scrollPane.setBounds(10, 20, 410, 200);
        add(scrollPane);

        findLabel.setBounds(10, 230, 350, 20);   
        
        JButton deleteButton = new JButton("삭제");
        deleteButton.setBounds(170, 280, 100, 30);
        deleteButton.addActionListener(this);
        add(findLabel);
        add(deleteButton);
        fdao.UpdateTable(tableModel);
        
        resultTable.addMouseListener(new MouseAdapter() { // 마우스 클릭 감지
            // 마우스 클랙에 관한 이벤트 처리
           @Override
            public void mouseClicked(MouseEvent e) {   
               
               
                if (e.getClickCount() == 2) {   // 2회 클릭시
                   // SwingUtilities : 마우스 및 키 이벤트 관련 메서드등 을 제공한다
                   if (SwingUtilities.isLeftMouseButton(e)) {   // 좌클릭 감지
                      int selectedRow = resultTable.getSelectedRow();
                      // 테이블에서 클릭한 행의 파일 경로 및 파일 이름 가져오기
                        String filePath = resultTable.getValueAt(selectedRow, 1).toString(); // 파일 경로 열의 인덱스
                        String fileName = resultTable.getValueAt(selectedRow, 2).toString(); // 파일명 열의 인덱스

                        // 파일 경로와 파일 이름을 합쳐서 새로운 파일 경로 생성
                        // File.separator는 파일 경로를 구성하는 데 사용되는 디렉토리 구분자(다른 운영체제 호환)
                        String fullPath = filePath + File.separator + fileName;

                        // 해당 파일 열기
                        openFile(fullPath);
                    } else if (SwingUtilities.isRightMouseButton(e)) { // 우클릭 감지
                       int selectedRow = resultTable.rowAtPoint(e.getPoint());
                        // 입력 다이얼로그 생성
                        String input = JOptionPane.showInputDialog(this, "값을 입력하세요:");
                        
                        // 다이얼로그에서 취소를 누르면 input은 null이 됨
                        if (input != null) {
                        	
                        	String selectedName = (String) tableModel.getValueAt(selectedRow, 2);
                           // JDBC 드라이버 로드.
                           fdao.updateNickName(input, selectedName);
                           fdao.UpdateTable(tableModel);
                        }
                    }
                }
            }
        });
    }


   @Override
   public void actionPerformed(ActionEvent e) {
      int selectedRowIndex = resultTable.getSelectedRow();
      tableModel = (DefaultTableModel)resultTable.getModel();
      String selectedName = (String) tableModel.getValueAt(selectedRowIndex, 2);
      if (selectedRowIndex != -1) { // 선택된 행이 있다면
          if (fdao.deletData(selectedName) > 0) { // 작업이 성공했을 경우
        	  fdao.UpdateTable(tableModel);	// 테이블 업데이트
          } else {
              System.out.println("삭제할 행이 없거나 삭제 중 오류가 발생했습니다.");
          }

      }else {
          // 선택된 행이 없을 때
         // JOptionPane.showMessageDialog() : 경고창 출력.
         // this : 부모 컴포넌트, 창 또는 프레임
         // JOptionPane.WARNING_MESSAGE : 경고 아이콘!!
         JOptionPane.showMessageDialog(this, "행을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
          System.out.println("선택한 행이 없습니다.");
      }
      
   }
   
   // 선택한 파일을 열때 사용하는 메서드
   private void openFile(String filePath) {
       try {
           File file = new File(filePath);
           if (Desktop.isDesktopSupported()) {
               Desktop.getDesktop().open(file);
           } else {
               System.out.println("운영체제가 해당 플랫폼을 지원하지 않습니다!!");
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
   // "파일명"을 기준으로 정렬하는 메서드
   public void sortTableByColumnName(String columnName, SortOrder order) {
	   // 결과 테이블의 모델을 가져옴
       DefaultTableModel model = (DefaultTableModel) resultTable.getModel();

       // Comparator를 사용하여 데이터를 정렬
       Comparator<Object[]> comparator = (o1, o2) -> {
    	   // 정렬할 열의 인덱스를 가져옴
           int columnIndex = getColumnIndexByName(columnName);
           
           // 정렬 기준이 되는 두 값(value1, value2)을 가져옴
           Comparable<Object> value1 = (Comparable<Object>) o1[columnIndex];
           Comparable<Object> value2 = (Comparable<Object>) o2[columnIndex];

           // 오름차순(ASCENDING) 또는 내림차순(DESCENDING)으로 비교하여 결과를 반환
           int result = order == SortOrder.ASCENDING ? value1.compareTo(value2) : value2.compareTo(value1);
           return result;
       };

       // 데이터를 배열로 변환
       Object[][] data = new Object[model.getRowCount()][model.getColumnCount()];
       for (int i = 0; i < model.getRowCount(); i++) {
           for (int j = 0; j < model.getColumnCount(); j++) {
               // 테이블 모델에서 데이터를 가져와서 새로운 배열에 저장
               data[i][j] = model.getValueAt(i, j);
           }
       }

        // 데이터 정렬
        Arrays.sort(data, comparator);

        // 테이블 모델 갱신
        //model.setDataVector(data, model.getColumnIdentifiers());
        // 테이블 모델 갱신
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                model.setValueAt(data[i][j], i, j);
            }
        }
    }
   // 열 이름을 통해 해당 열의 인덱스를 반환하는 메서드
   public int getColumnIndexByName(String columnName) {
        // 테이블의 모든 열을 순회하며 주어진 열 이름과 일치하는 열을 찾아 인덱스 반환
        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            if (resultTable.getColumnName(i).equals(columnName)) {
                return i; // 일치하는 열의 인덱스 반환
            }
        }
        
        return -1; // 해당하는 열 이름을 찾지 못한 경우
    }
    
   //정렬에 맞는 버튼을 눌렀을때 정렬이 될수있도록 하는 메서드
   public void handles() {
	   switch(isMenu) {
           case 0 : 
              //그 버튼을 눌렀는지 알수있게 버튼색을 변경
              jmiDataSort.setForeground(Color.BLACK); 
              jmiNameSort.setForeground(Color.RED);
              jmiDateSort.setForeground(Color.BLACK);
              
              //내림차순이 선택되어 있는지
              if(jmciDesc.isSelected()==true) {
                 sortTableByColumnName("파일명", SortOrder.DESCENDING);
              }
              //오름차순이 선택되어 있는지
              if(jmciAsc.isSelected()==true){
                 sortTableByColumnName("파일명", SortOrder.ASCENDING);
              }  
              break;
         
           case 1 : 
              jmiDataSort.setForeground(Color.BLACK);
              jmiNameSort.setForeground(Color.BLACK);
              jmiDateSort.setForeground(Color.RED);
              if(jmciDesc.isSelected()==true) {
                 sortTableByColumnName("수정 시간", SortOrder.DESCENDING);
              }
              if(jmciAsc.isSelected()==true){
                 sortTableByColumnName("수정 시간", SortOrder.ASCENDING);
              }  
              break;
         
           case 2 : 
              jmiDataSort.setForeground(Color.RED);
              jmiNameSort.setForeground(Color.BLACK);
              jmiDateSort.setForeground(Color.BLACK);
              if(jmciDesc.isSelected()==true) {
                 sortTableByColumnName("크기", SortOrder.DESCENDING);
              }
              if(jmciAsc.isSelected()==true){
                 sortTableByColumnName("크기", SortOrder.ASCENDING);
              }  
              break;
       }
    }
}
class GUIClass2 extends JFrame{
	public GUIClass2(){
    // 컨테이너 생성
    Container ct = getContentPane();
            
      // JTabbedPane() 객체 생성   
      JTabbedPane jtp = new JTabbedPane();
      
      // 세이브 판넬 및 파인트 판넬 객체 생성.
      SavePanel sp = new SavePanel();
      FindPanel fp = new FindPanel();
      
      // JTabbedPane()에 판넬 추가       
      jtp.addTab("탐색", fp);
      jtp.addTab("찾기", sp);
            
      // 컨테이너에 추가               
      ct.add(jtp);
            
      setTitle("Finder");
      setSize(450, 400);
               
      // 윈도우 창 종료시 프로세스 닫기
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                             
      // 프레임을 화면에 출력
      setVisible(true);
   }
}
public class MainFinder {
   public static void main(String[] args) {
      new GUIClass2();
   }
}

