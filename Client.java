import java.io.*;
import java.net.*;
import java.nio.channels.*;

public class Client {
    public static void main(String[] args) throws IOException {
        String serverAddress = "<IP Server>"; // IP ของ Server 
        int port = <port Server>; // พอร์ตที่ Server ใช้
        String outputFilePathClassic = "<path file>"; // ที่เก็บไฟล์ที่ได้รับจาก
                                                                                        // Classic
        String outputFilePathZeroCopy = "<path file>"; // ที่เก็บไฟล์ที่ได้รับจาก
                                                                                           // Zero Copy

    
        // เชื่อมต่อครั้งที่สองเพื่อรับไฟล์แบบ Zero Copy
        try (Socket zeroCopySocket = new Socket(serverAddress, port)) {
            // ส่งข้อความ ZeroCopy ไปที่ Server
            DataInputStream disZeroCopy = new DataInputStream(zeroCopySocket.getInputStream()); //รับชื่อไฟล์
            String receivedFileName = disZeroCopy.readUTF();

            DataOutputStream dosZeroCopy = new DataOutputStream(zeroCopySocket.getOutputStream());
            dosZeroCopy.writeUTF("ZeroCopy");
            dosZeroCopy.flush();
            
            System.out.println("Receiving Zero Copy File...");
            receiveFileZeroCopy(zeroCopySocket, outputFilePathZeroCopy+"_zeroCopy_"+receivedFileName);
        }

         try (Socket classicSocket = new Socket(serverAddress, port)) {
            // ส่งข้อความ Classic ไปที่ Server
            DataInputStream disClassic = new DataInputStream(classicSocket.getInputStream()); //รับชื่อไฟล์
            String receivedFileName = disClassic.readUTF();

            DataOutputStream dosClassic = new DataOutputStream(classicSocket.getOutputStream());
            dosClassic.writeUTF("Classic");
            dosClassic.flush();
            
            System.out.println("Receiving Classic File...");
            receiveFileClassic(classicSocket, outputFilePathClassic+"_classic_"+receivedFileName);
        }

    }

    private static void receiveFileClassic(Socket socket, String outputFilePath)
            throws IOException {
                long startTime = System.currentTimeMillis();
                InputStream is = socket.getInputStream();
                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    byte[] buffer = new byte[64 * 1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.flush();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Classic File received and saved as " + outputFilePath);
                    System.out.println("Classic file received completed in " + (endTime - startTime) + " ms");
                }
    }

    private static void receiveFileZeroCopy(Socket socket, String outputFilePath) throws IOException {
        ReadableByteChannel socketChannel = Channels.newChannel(socket.getInputStream());
        long startTime = System.currentTimeMillis();
        
            FileChannel fileChannel = new FileOutputStream(outputFilePath).getChannel();
            fileChannel.transferFrom(socketChannel, 0, Long.MAX_VALUE); // Zero Copy
            System.out.println("Zero Copy File received and saved as " + outputFilePath);
        
        long endTime = System.currentTimeMillis();
        System.out.println("Zero Copy file received completed in " + (endTime - startTime) + " ms");
    }
}