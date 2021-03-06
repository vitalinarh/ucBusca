package ucBusca.action;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

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

public class FacebookRestAction extends ActionSupport implements SessionAware {

    private Map<String, Object> session;
    private String code = null;
    private String facebookId = null;
    private int facebookIdnum;

    @Override
    public String execute() throws Exception {

        if (this.code != null) {
            facebookId = this.getUcBuscaBean().getFacebookAuth2(this.code);
            if(facebookId != null) {
                session.put("facebookName", facebookId);
                session.put("isAdmin", false);
                session.put("isLogged", true);
                System.out.println(facebookId);
                facebookIdnum = Integer.parseInt(facebookId.split("id")[1].substring(3, 8));
                String name = facebookId.split(",")[0].substring(9, facebookId.split(",")[0].length() - 1);
                session.put("username", name);
                this.getUcBuscaBean().setClientId(facebookIdnum);
                return SUCCESS;
            }
        }
        return ERROR;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UcBuscaBean getUcBuscaBean() throws RemoteException {
        if(!session.containsKey("ucBuscaBean"))
            this.setUcBuscaBean(new UcBuscaBean());
        return (UcBuscaBean) session.get("ucBuscaBean");
    }

    public void setUcBuscaBean(UcBuscaBean ucBuscaBean) {
        this.session.put("ucBuscaBean", ucBuscaBean);
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}