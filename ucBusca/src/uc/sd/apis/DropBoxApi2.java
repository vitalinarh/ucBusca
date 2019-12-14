package uc.sd.apis;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.AccessTokenExtractor;
import com.github.scribejava.core.extractors.JsonTokenExtractor;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.Verb;

// Adapted from https://thysmichels.com/2014/06/04/dropbox-oauth-2-0-scribe-java-example/
public class DropBoxApi2 extends DefaultApi20 {
	
	    @Override
	    public String getAccessTokenEndpoint() {
	        return "https://api.dropbox.com/1/oauth2/token?grant_type=authorization_code";
	    }
	 
	    @Override
	    public String getAuthorizationUrl(OAuthConfig config) {
	        return String.format("https://www.dropbox.com/1/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=%s", config.getApiKey(), config.getCallback());
	    }
	 
	     @Override
	     public Verb getAccessTokenVerb(){
	           return Verb.POST;
	     }
	      
	    @Override
	    public AccessTokenExtractor getAccessTokenExtractor() {
	        return new JsonTokenExtractor();
	    }
}
