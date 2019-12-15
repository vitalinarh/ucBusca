package ucBusca.action;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Scanner;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.Verifier;
import com.github.scribejava.core.oauth.OAuthService;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import org.omg.PortableInterceptor.SUCCESSFUL;
import uc.sd.apis.FacebookApi2;
import ucBusca.model.UcBuscaBean;

public class FacebookRestClient extends ActionSupport implements SessionAware {

  private Map<String, Object> session;
  private String authorizationUrl = null;

  @Override
  public String execute() throws Exception {

    this.authorizationUrl = this.getUcBuscaBean().getFacebookAuth();

    return SUCCESS;

  }

  public UcBuscaBean getUcBuscaBean() throws RemoteException {
    if(!session.containsKey("ucBuscaBean"))
      this.setUcBuscaBean(new UcBuscaBean());
    return (UcBuscaBean) session.get("ucBuscaBean");
  }

  public void setAuthorizationUrl(String authorizationUrl) {
    this.authorizationUrl = authorizationUrl; // what about this input?
  }

  public String getAuthorizationUrl() {
    return this.authorizationUrl; // what about this input?
  }

  public void setUcBuscaBean(UcBuscaBean ucBuscaBean) {
    this.session.put("ucBuscaBean", ucBuscaBean);
  }

  @Override
  public void setSession(Map<String, Object> session) {
    this.session = session;
  }
}