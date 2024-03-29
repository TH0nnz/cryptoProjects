package ApplePay.ApplePayMerchantIdValidate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class ApplePayMerchantIdValidater {
	private static final String ROOT_CER = "D:\\xxxxxx\\xxxxxx\\xxxxxx\\";
	private static final String MERCHANT_ID_P12 = "ApplePayMerchantId.p12";
	private static final String MERCHANT_ID_PASS = "xxxxxx";

	
	public String validate(String validationURL) {

//		String validationURL = "https://apple-pay-gateway-cert.apple.com/paymentservices/startSession";
		URL url;
		String UTF8 = "UTF-8";
		String merchantIdentifier = "xxxxxx";
		String domainName = "xxxxxx";
		String displayName = "xxxxxx";


		try {
			String jsonBody = String.format("{\"merchantIdentifier\":\"%s\",\"domainName\":\"%s\",\"displayName\":\"%s\"}",
					URLEncoder.encode(merchantIdentifier, UTF8), URLEncoder.encode(domainName, UTF8),
					URLEncoder.encode(displayName, UTF8));
//			System.out.println("query");
//			System.out.println(query);
			byte[] postData = jsonBody.getBytes(UTF8);
			int postDataLength = postData.length;

			url = new URL(validationURL);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			con.setDoOutput(true);
			con.setDoInput(true);

			File pKeyFile = new File(ROOT_CER + MERCHANT_ID_P12);

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			InputStream keyInput = new FileInputStream(pKeyFile);
			keyStore.load(keyInput, MERCHANT_ID_PASS.toCharArray());
			keyInput.close();
			keyManagerFactory.init(keyStore, MERCHANT_ID_PASS.toCharArray());
			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
			SSLSocketFactory sockFact = context.getSocketFactory();
			con.setSSLSocketFactory(sockFact);
			// ��X
			OutputStream os = con.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(jsonBody);
			bw.flush();
			bw.close();
			osw.close();
			os.close();
			// ��J
			InputStream in = con.getInputStream();
			InputStreamReader isr = new InputStreamReader(in, UTF8);
			BufferedReader br = new BufferedReader(isr);
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			br.close();
			isr.close();
			in.close();

			System.out.println("uew POST meyhod get apple merchant object�G " + sb.toString());
			return sb.toString();

			/**
			 * apple merchant json body�G
			 * {
			 * "epochTimestamp": 164967xxxxxx9,
			 * "expiresAt": 16496xxxxxx99,
			 * "merchantSessionIdentifier":"SSH0C93067174574xxxxxx8DAxxxxxx6xxxxxxxxxxxxED1343Fxxxxxx5E12BEE925xxxxxx1A17C46B0DE5A943F0F94927C24",
			 * "nonce": "774a8c10",
			 * "merchantIdentifier":"03047015415Bxxxxxx4E37536EAxxxxxx7A2E06xxxxxx42CB6xxxxxx16xxxxxx",
			 * "domainName": "xxxxxx",
			 * "displayName": "xxxxxxTest",
			 * "signature": "308006092a864886f70d010702axxxxxx0020101310f300d0xxxxxx648016503040xxxxxx0308006092xxxxxx6f70d0107010000a080308203e43082038ba003020102020859d8a1bxxxxxx3cd300a06082a8648ce3d040302307a312e302c06035504030c2541xxxxxx65204170706c69636174696f6e20496e746567726174696f6e204341202d20473331263024060355040b0c1d41xxxxxx652043657274696669636174696f6e20417574686f726974793113301106035504xxxxxx4170706c6520496e632e310b3009060355040613025553301e170d3231303432303139333730305a170d3236303431393139333635395a30623128302606035504030c1f6563632d736d702d62726f6b65722d7369676e5f5543342d53414e44424f5831143012060355040b0c0b694f532053797374656d7331133011060355040a0c0a4170706c6520496e632e310b30090603550406130255533059301306072a8648ce3d020106082a8648ce3d030107034200048230fdabc39cf75e202c50d99b4512e637e2a901dd6cb3e0b1cd4b526798f8cf4ebde81a25a8c21e4c33ddce8e2a96c2f6afa1930345c4e87a4426ce951b1295a38202113082020d300c0603551d130101ff04023000301f0603551d2304183016801423f249c44f93e4ef27e6c4f6286c3fa2bbfd2e4b304506082b0601050507010104393037303506082b060105050730018629687474703a2f2f6f6373702e6170706c652e636f6d2f6f63737030342d6170706c65616963613330323082011d0603551d2004820114308201103082010c06092a864886f7636405013081fe3081c306082b060105050702023081b60c81b352656c69616e6365206f6e207468697320636572746966696361746520627920616e7920706172747920617373756d657320616363657074616e6365206f6620746865207468656e206170706c696361626c65207374616e64617264207465726d7320616e6420636f6e646974696f6e73206f66207573652c20636572746966696361746520706f6c69637920616e642063657274696669636174696f6e2070726163746963652073746174656d656e74732e303606082b06010505070201162a687474703a2f2f7777772e6170706c652e636f6d2f6365727469666963617465617574686f726974792f30340603551d1f042d302b3029a027a0258623687474703a2f2f63726c2e6170706c652e636f6d2f6170706c6561696361332e63726c301d0603551d0e041604140224300b9aeeed463197a4a65a299e4271821c45300e0603551d0f0101ff040403020780300f06092a864886f76364061d04020500300a06082a8648ce3d0403020347003044022074a1b324db4249430dd3274c5074c4808d9a1f480e3a85c5c1362566325fbca3022069369053abf50b5a52f9f6004dc58aad6c50a7d608683790e0a73ad01e4ad981308202ee30820275a0030201020208496d2fbf3a98da97300a06082a8648ce3d0403023067311b301906035504030c124170706c6520526f6f74204341202d20473331263024060355040b0c1d4170706c652043657274696669636174696f6e20417574686f7269747931133011060355040a0c0a4170706c6520496e632e310b3009060355040613025553301e170d3134303530363233343633305a170d3239303530363233343633305a307a312e302c06035504030c254170706c65204170706c69636174696f6e20496e746567726174696f6e204341202d20473331263024060355040b0c1d4170706c652043657274696669636174696f6e20417574686f7269747931133011060355040a0c0a4170706c6520496e632e310b30090603550406130255533059301306072a8648ce3d020106082a8648ce3d03010703420004f017118419d76485d51a5e25810776e880a2efde7bae4de08dfc4b93e13356d5665b35ae22d097760d224e7bba08fd7617ce88cb76bb6670bec8e82984ff5445a381f73081f4304606082b06010505070101043a3038303606082b06010505073001862a687474703a2f2f6f6373702e6170706c652e636f6d2f6f63737030342d6170706c65726f6f7463616733301d0603551d0e0416041423f249c44f93e4ef27e6c4f6286c3fa2bbfd2e4b300f0603551d130101ff040530030101ff301f0603551d23041830168014bbb0dea15833889aa48a99debebdebafdacb24ab30370603551d1f0430302e302ca02aa0288626687474703a2f2f63726c2e6170706c652e636f6d2f6170706c65726f6f74636167332e63726c300e0603551d0f0101ff0404030201063010060a2a864886f7636406020e04020500300a06082a8648ce3d040302036700306402303acf7283511699b186fb35c356ca62bff417edd90f754da28ebef19c815e42b789f898f79b599f98d5410d8f9de9c2fe0230322dd54421b0a305776c5df3383b9067fd177c2c216d964fc6726982126f54f87a7d1b99cb9b0989216106990f09921d00003182018b30820187020101308186307a312e302c06035504030c254170706c65204170706c69636174696f6e20496e746567726174696f6e204341202d20473331263024060355040b0c1d4170706c652043657274696669636174696f6e20417574686f7269747931133011060355040a0c0a4170706c6520496e632e310b3009060355040613025553020859d8a1bcaaf4e3cd300d06096086480165030402010500a08195301806092a864886f70d010903310b06092a864886f70d010701301c06092a864886f70d010905310f170d3232303431313039343134325a302a06092a864886f70d010934311d301b300d06096086480165030402010500a10a06082a8648ce3d040302302f06092a864886f70d01090431220420377d02e3ef8ac1c4509bff967e279ab25adafbd4ca62a298eee8a9e8c96086df300a06082a8648ce3d0403020446304402200222ad427650c7b4fb712c46ba18d2168e36aa1aa25f32293252267db8fb981c02204892dc3c470f816c0193fa88cfedc358573218524c3c8727e5b02565040ebae5000000000000",
			 * "operationalAnalyticsIdentifier": "xxxxxx:03047xxxxxxBCxxxxxxE37xxxxxxD03C7A2E06F0AEBxxxxxx06312Dxxxxxx55",
			 * "retries": 0,
			 * "pspId": "03047015415BC3CE434ExxxxxxAF6D03Cxxxxxxxxxxxx42CB6xxxxxx16409155"
			 * }
			 * 
			 */

		} catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException
				| UnrecoverableKeyException | CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
