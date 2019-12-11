package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.util.Map;

public class LogoutAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;
    private String username = null, password = null;

    @Override
    public String execute() throws Exception {

        this.session.put("isLogged", false);
        this.session.put("isAdmin", false);
        this.username = null;
        this.password = null;

        return SUCCESS;

    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
