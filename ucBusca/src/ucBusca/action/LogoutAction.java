package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.util.Map;

/**
 * Class that's responsible for logging out the user.
 * To do that, it updates all the session variables to reflect an anonymous user.
 */
public class LogoutAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    /**
     * User's session.
     */
    private Map<String, Object> session;
    /**
     * User's credentials.
     */
    private String username = null, password = null;

    /**
     * Execute: Updates all the session variables to reflect an anonymous user.
     * @return String
     * @throws Exception
     */
    @Override
    public String execute() throws Exception {

        this.session.put("isLogged", false);
        this.session.put("isAdmin", false);
        this.session.put("userId", 0);
        this.session.put("searchHistory", null);
        this.session.put("searchResults", null);

        this.username = null;
        this.password = null;

        return SUCCESS;

    }

    /**
     * Sets the user's session.
     * @param session
     */
    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
