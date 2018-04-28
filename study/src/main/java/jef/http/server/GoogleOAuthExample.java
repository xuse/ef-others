//package jef.http.server;
//
//import java.security.KeyFactory; 
//import java.security.PrivateKey; 
//import java.security.spec.EncodedKeySpec; 
//import java.security.spec.PKCS8EncodedKeySpec; 
//import java.util.Collection; 
//import java.util.Map; 
//import com.google.gdata.client.GoogleService; 
//import com.google.gdata.client.authn.oauth.OAuthParameters; 
//import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer; 
//import com.google.gdata.client.authn.oauth.OAuthSigner; 
//import com.google.gdata.data.BaseEntry; 
//import com.google.gdata.data.BaseFeed; 
//import com.google.gdata.data.Feed; 
//import net.oauth.OAuth; 
//import net.oauth.OAuthConsumer; 
//import net.oauth.OAuthMessage; 
//import net.oauth.OAuthServiceProvider; 
//
//import net.oauth.signature.OAuthSignatureMethod; 
//import net.oauth.signature.RSA_SHA1; 
//
//public class GoogleOAuthExample { 
//   //Note, use the private key of your self-signed X509 certificate. 
//   private static final String PRIVATE_KEY =  "XXXXXXXX" ; 
//
//   public static void main(String[] args) throws Exception { 
//       KeyFactory fac = KeyFactory.getInstance( RSA ); 
//       //PRIVATE_KEY is the private key of your self-signed X509 certificate. 
//       EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec( 
//		    OAuthSignatureMethod.decodeBase64(PRIVATE_KEY)); 
//       fac = KeyFactory.getInstance( RSA ); 
//       PrivateKey privateKey = fac.generatePrivate(privKeySpec); 
//       OAuthServiceProvider serviceProvider = new OAuthServiceProvider( 
//           //used for obtaining a request token 
//			 // https://www.google.com/accounts/OAuthGetRequestToken , 
//	        //used for authorizing the request token 
//            https://www.google.com/accounts/OAuthAuthorizeToken , 
//            //used for upgrading to an access token 
//            https://www.google.com/accounts/OAuthGetAccessToken ); 
//
//       OAuthConsumer oauthConsumer = new OAuthConsumer(null 
//           ,  lszhy.weebly.com  //consumer key 
//           ,  hIsGnM+T4+86fKNesUtJq7Gs  //consumer secret 
//           , serviceProvider); 
//
//       oauthConsumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1); 
//       oauthConsumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey); 
//
//       DesktopClient client = new DesktopClient(oauthConsumer); 
//       client.setOAuthClient(new OAuthClient(new HttpClient4())); 
//		
//       Collection&lt;? extends Map.Entry&gt; parameters = 
//		    OAuth.newList( scope , http://www.google.com/calendar/feeds/ );
//		
//       String accessToken = client.getAccessToken(OAuthMessage.GET,parameters); 
//		
//		
//       //Make an OAuth authorized request to Google 
//		
//       // Initialize the variables needed to make the request 
//       URL feedUrl = new URL( 
//		     http://www.google.com/calendar/feeds/default/allcalendars/full );
//       
//       System.out.println( Sending request to   + feedUrl.toString()); 
//       System.out.println(); 
//       
//       GoogleService googleService = new GoogleService( cl ,  oauth-sample-app ); 
//
//       OAuthSigner signer = new OAuthRsaSha1Signer(MyGoogleService.PRIVATE_KEY); 
//       
//       // Set the OAuth credentials which were obtained from the step above. 
//       OAuthParameters para = new OAuthParameters(); 
//       para.setOAuthConsumerKey( lszhy.weebly.com ); 
//       para.setOAuthToken(accessToken); 
//       googleService.setOAuthCredentials(para, signer); 
//       
//       // Make the request to Google 
//       BaseFeed resultFeed = googleService.getFeed(feedUrl, Feed.class); 
//       System.out.println( Response Data: );               
//       System.out.println( ========================================== ); 
//
//       System.out.println( |TITLE:   + resultFeed.getTitle().getPlainText()); 
//       if (resultFeed.getEntries().size() == 0) { 
//          System.out.println( |\tNo entries found. ); 
//       } else { 
//           for (int i = 0; i &lt; resultFeed.getEntries().size(); i++) { 
//              BaseEntry entry = (BaseEntry) resultFeed.getEntries().get(i); 
//              System.out.println( |\t  + (i + 1) +  :  
//                   + entry.getTitle().getPlainText()); 
//           } 
//       } 
//       System.out.println( ========================================== ); 	
//   } 
//} 