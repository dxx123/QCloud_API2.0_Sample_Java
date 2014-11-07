import java.awt.EventQueue;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.swing.JTextArea;


public class SignTest {

	private JFrame frmQcloud;
	private JTextField textID;
	private JTextField textKEY;
	private JLabel url;
	private JTextField textURL;
	private JTextArea textArea;

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SignTest window = new SignTest();
					window.frmQcloud.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public SignTest() {
		initialize();
	}

	private void initialize() {				
		
		frmQcloud = new JFrame();
		frmQcloud.setResizable(false);
		frmQcloud.setTitle("QCLOUD API TEST");
		frmQcloud.setBounds(100, 100, 568, 427);
		frmQcloud.getContentPane().setLayout(null);
		
		JLabel lblSECRET_id = new JLabel("SECRET_ID:");
		lblSECRET_id.setBounds(10, 32, 88, 15);
		frmQcloud.getContentPane().add(lblSECRET_id);
		
		textID = new JTextField();
		textID.setBounds(123, 29, 429, 21);
		frmQcloud.getContentPane().add(textID);
		textID.setColumns(10);
		
		JLabel lblSECRET_Key = new JLabel("SECRET_KEY:");
		lblSECRET_Key.setBounds(10, 57, 88, 15);
		frmQcloud.getContentPane().add(lblSECRET_Key);
		
		textKEY = new JTextField();
		textKEY.setBounds(123, 54, 429, 21);
		frmQcloud.getContentPane().add(textKEY);
		textKEY.setColumns(10);
		
		url = new JLabel("URL:");
		url.setBounds(27, 7, 54, 15);
		frmQcloud.getContentPane().add(url);
		
		textURL = new JTextField();
		textURL.setBounds(123, 4, 429, 21);
		frmQcloud.getContentPane().add(textURL);
		textURL.setColumns(10);
		
		JButton btnCall = new JButton("CALL");
		
		btnCall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreeMap<String, Object> requestParams = new TreeMap<String, Object>();
				
				String SECRET_ID=textID.getText();
				String SECRET_KEY=textKEY.getText();
				
		        requestParams.put("SecretId", SECRET_ID);
		        requestParams.put("Region", "sh");
		        requestParams.put("Timestamp", System.currentTimeMillis() / 1000);
		        Random rand = new Random();
		        requestParams.put("Nonce", rand.nextInt(java.lang.Integer.MAX_VALUE));

		        requestParams.put("Action", "DescribeInstances");

		        String requestMethod = "POST";
		        String requestHost = "cvm.api.qcloud.com";
		        String requestPath = "/v2/index.php";

		        try {
		            String plainText = QcloudSign.makeSignPlainText(requestParams, requestMethod, requestHost, requestPath);
		            String sign = QcloudSign.sign(plainText, SECRET_KEY);
		            textURL.setText("т╜нд: " + plainText);
		            textArea.setText(sign);		          
		            if (requestMethod.equals("GET")) {
		                requestParams.put("Signature", java.net.URLEncoder.encode(sign, "UTF-8"));
		            } else {
		                requestParams.put("Signature", sign);
		            }

		            String retStr = _sendRequest("https://" + requestHost + requestPath, requestParams, requestMethod);
		            //System.out.println(retStr);
		            textArea.setText(retStr);

		        } catch(Exception we) {
		            System.out.println(we);
		        }
			}
		});
		btnCall.setBounds(459, 86, 93, 23);
		frmQcloud.getContentPane().add(btnCall);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBounds(10, 117, 542, 271);
		frmQcloud.getContentPane().add(textArea);
		frmQcloud.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	protected static String _sendRequest(String url,
            Map<String, Object> requestParams, String requestMethod)
    {
        String result = "";
        BufferedReader in = null;
        String paramStr = "";

        for(String key: requestParams.keySet()) {
            if (!paramStr.isEmpty()) {
                paramStr += '&';
            }
            paramStr += key + '=' + requestParams.get(key).toString();
        }

        try {

            if (requestMethod.equals("GET")) {
                if (url.indexOf('?') > 0)
                {
                    url += '&' + paramStr;
                } else {
                    url += '?' + paramStr;
                }
            }

            URL realUrl = new URL(url);
            URLConnection connection = null;
            if (url.substring(0, 5).toUpperCase().equals("HTTPS")) {
                HttpsURLConnection httpsConn = (HttpsURLConnection)realUrl.openConnection();

                httpsConn.setHostnameVerifier(new HostnameVerifier(){
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                connection = httpsConn;
            } else {
                connection = realUrl.openConnection();
            }

           
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            if (requestMethod.equals("POST")) {
                
                connection.setDoOutput(true);
                connection.setDoInput(true);
               
                PrintWriter out = new PrintWriter(connection.getOutputStream());
                
                out.print(paramStr);
               
                out.flush();
            }

            
            connection.connect();

           
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
}
