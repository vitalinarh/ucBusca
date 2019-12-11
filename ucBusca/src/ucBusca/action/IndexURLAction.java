package ucBusca.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;
import ucBusca.model.UcBuscaBean;

import java.util.Map;

public class IndexURLAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 5590830L;
    private Map<String, Object> session;
    private String urlToIndex = null;

    @Override
    public String execute() throws Exception {
        if(this.urlToIndex != null){
            String response = this.getUcBuscaBean().getURLIndexed(this.urlToIndex);

            this.session.put("response", response);

            return SUCCESS;
        }

        return INPUT;
    }

    public void setUrlToIndex(String urlToIndex) { this.urlToIndex = urlToIndex; }

    public UcBuscaBean getUcBuscaBean() {
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
