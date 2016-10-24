<?php
   //client id and client secret
   $client = '<put your client id from the App Settings here>';
   $secret = '<put your client secret from the App Settings here>';

   // the full url to redirect to this file
   $url = get_this_url();

   $f = array( 'code' => FILTER_SANITIZE_STRING, 'access_token' => FILTER_SANITIZE_STRING );
   $request = filter_input_array(INPUT_GET, $f);

   //STEP 1 - Get Access Code
   if(!isset($request['code']) && !isset($request['access_token']))
   {
     header( 
      "Location: https://graph.api.smartthings.com/oauth/authorize?response_type=code&client_id=$client&redirect_uri=".$url."&scope=app" 
      );
   }
   //STEP 2 - Use Access Code to claim Access Token
   else if(isset($request['code']))
   {
	   $code = $request['code'];
      $base = "https://graph.api.smartthings.com/oauth/token";
      $page = 
       "$base?grant_type=authorization_code&client_id=".$client."&client_secret=".$secret."&redirect_uri=".$url."&code=".$code."&scope=app";
      $ch = curl_init();
      curl_setopt($ch, CURLOPT_URL,            $page );
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1 );
      curl_setopt($ch, CURLOPT_POST,           0 );
      curl_setopt($ch, CURLOPT_HTTPHEADER,     array('Content-Type: application/json')); 
      $response =  json_decode(curl_exec($ch),true);
      curl_close($ch);
      if(isset($response['access_token']))
      {
         //Redirect to self with access token for step 3 for ease of bookmarking
         header( "Location: ?access_token=".$response['access_token'] ) ;
      }
      else
      {
         print "error requesting access token...";
         print_r($response);
      }
   }
   //Step 3 - Lookup Endpoint and write out urls
   else if(isset($request['access_token']))
   {
      $url = "https://graph.api.smartthings.com/api/smartapps/endpoints/$client?access_token=".$request['access_token'];
      $json = implode('', file($url));
      $theEndpoints = json_decode($json,true);
      print "<html><head><style>h3{margin-left:10px;}a:hover{background-color:#c4c4c4;} a{border:1px solid black; padding:5px; margin:5px;text-decoration:none;color:black;border-radius:5px;background-color:#dcdcdc}</style></head><body>";
      print "<i>Save the above URL (access_token) for future reference.</i>";
      print " <i>Right Click on buttons to copy link address.</i>";
   }

   function safe_server_var( $var, $op = FILTER_SANITIZE_STRING )
   {
      if (filter_has_var(INPUT_SERVER, $var))
      {
         $ret_val = filter_input(INPUT_SERVER, $var, $op, FILTER_NULL_ON_FALURE);
      }
      else
      {
         $ret_val = "";
      }
      return $ret_val;
   }

   function get_server_home($server_port = 0)
   {
      $sname = safe_server_var("SERVER_NAME");
      $addr = safe_server_var("SERVER_ADDR");
      return ( make_url($sname != "" ? $sname : $addr, $server_port) );
   }

   function make_url($server_addr, $server_port = 0)
   {
      $proto = "http://";
      $sproto = safe_server_var("HTTPS", FILTER_UNSAFE_RAW);
      if ($sproto != "")
      {
         $proto = "https://";
      }
      $sport = safe_server_var("SERVER_PORT");
      $port = $server_port == 0 ? $sport : $server_port;
      if (($proto == "http://" && $port == 80) || ($proto == "https://" && $port == 443))
      {
         return ( $proto . $server_addr );
      }
      else
      {
         return ( $proto . $server_addr . ":" . $port);
      }
   }

   function get_this_url()
   {
      $page = safe_server_var("PHP_SELF");
      return get_server_home() . htmlspecialchars($page);
   }

?>
   
