import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) throws IOException {
        String filePath = "<Path>"; // กำหนดเส้นทางไฟล์ที่จะแชร์
        int port = <Port Number>; // กำหนดport

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            // รอการเชื่อมต่อจาก Client
            while (true) {
                Socket socket = serverSocket.accept();

                // สร้างเทรดใหม่สำหรับการจัดการการเชื่อมต่อ
                new Thread(new ClientHandler(socket, filePath)).start();
            }
        }
    }

    // คลาสที่ใช้ในการจัดการการเชื่อมต่อแต่ละ Client
    static class ClientHandler implements Runnable {
        private Socket socket;
        private String filePath;

        public ClientHandler(Socket socket, String filePath) {
            this.socket = socket;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            try {
                 // รับข้อความจาก Client เพื่อระบุประเภทการถ่ายโอน
                 DataInputStream dis = new DataInputStream(socket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                 // ส่งชื่อไฟล์ที่ส่งไป (ชื่อไฟล์พร้อมนามสกุล)
                String fileName = "<File Name>";  // เปลี่ยนตามชื่อไฟล์ที่ต้องการ
                dos.writeUTF(fileName);  // ส่งชื่อไฟล์ไปยัง Client
                dos.flush();

                String transferType = dis.readUTF();
 
                 // ตรวจสอบประเภทการถ่ายโอน
                 if ("Classic".equalsIgnoreCase(transferType)) {
                     System.out.println("Client requested Classic Transfer.");
                     transferFileClassic(socket, filePath);
                 } else if ("ZeroCopy".equalsIgnoreCase(transferType)) {
                     System.out.println("Client requested Zero Copy Transfer.");
                     transferFileZeroCopy(socket, filePath);
                 } else {
                     System.out.println("Unknown transfer type requested: " + transferType);
                 }
 
                 socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private static void transferFileClassic(Socket socket, String filePath) throws IOException {
            System.out.println("Starting Classic File Transfer...");
            long startTime = System.currentTimeMillis();
            OutputStream os = socket.getOutputStream();
            try (FileInputStream fis = new FileInputStream(filePath);) {

                byte[] buffer = new byte[32 * 1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Classic File Transfer completed in " + (endTime - startTime) + " ms");
        }

        private static void transferFileZeroCopy(Socket socket, String filePath) throws IOException {
            System.out.println("Starting Zero Copy File Transfer...");
            long startTime = System.currentTimeMillis();
            WritableByteChannel socketChannel = Channels.newChannel(socket.getOutputStream());
            try (FileChannel fileChannel = new FileInputStream(filePath).getChannel();) {
                fileChannel.transferTo(0, fileChannel.size(), socketChannel); // Zero Copy
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Zero Copy File Transfer completed in " + (endTime - startTime) + " ms");
        }
    }
}