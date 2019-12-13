package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.rmi.RemoteException;
import java.util.Map;

public class LoginAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;
    private String username = null, password = null;

    @Override
    public String execute() throws Exception {
        if( (this.username != null && !username.equals("")) && (this.password != null && !password.equals("")) ) {
            int loginStatus = this.getUcBuscaBean().getLogin(this.username, this.password);

            switch(loginStatus) {
                case (-1): //incorrect credentials
                    session.put("isLogged", false);
                    session.put("isAdmin", false);

                    return NONE;

                case (0): //standard user
                    session.put("isLogged", true);
                    session.put("isAdmin", false);

                    session.put("username", this.username);

                    return SUCCESS;

                case (1): //admin user
                    session.put("isLogged", true);
                    session.put("isAdmin", true);

                    session.put("username", this.username);

                    return SUCCESS;

                default: //qualquer outra situação
                    return ERROR;
            }
        }
        else {
            this.session.put("isLogged", false);
            return INPUT;
        }
    }

    public void setUsername(String username) {
        this.username = username; // will you sanitize this input? maybe use a prepared statement?
    }

    public void setPassword(String password) {
        this.password = password; // what about this input?
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
