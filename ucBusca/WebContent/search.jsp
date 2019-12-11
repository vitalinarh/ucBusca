<?xml version="1.0" encoding="UTF-8"?>

<!-- The core configuration file for the framework is the default (struts.xml) file
and should reside on the classpath of the webapp (generally /WEB-INF/classes). -->

<!DOCTYPE struts PUBLIC
"-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"
"http://struts.apache.org/dtds/struts-2.0.dtd">

<struts>

    <!-- devMode equals debug information and reload everything for every request -->
    <constant name="struts.devMode" value="true" />
    <constant name="struts.ui.theme" value="simple" />

    <package name="ucBusca" extends="struts-default">

        <default-action-ref name="menu" />

        <global-results>
            <result name="error">/error.jsp</result>
        </global-results>

        <!-- all exceptions not caught by the application will lead to error.jsp -->
        <global-exception-mappings>
            <exception-mapping exception="java.lang.Exception" result="error" />
        </global-exception-mappings>

        <!-- 'index' ucBusca.action leads to the view provided by index.jsp -->
        <action name="menu">
            <result>/menu.jsp</result>
        </action>

        <!-- 'login' action calls 'execute' in 'LoginAction' -->
        <action name="login" class="ucBusca.action.LoginAction" method="execute">
            <result name="success">/menu.jsp</result>
            <result name="input">/login.jsp</result>
            <result name="error">/login.jsp</result>
            <result name="none">/login.jsp</result>
        </action>

        <!-- 'indexURL' action calls 'execute' in 'indexURLAction' -->
        <action name="indexURL" class="ucBusca.action.IndexURLAction" method="execute">
            <result name="success">/menu.jsp</result>
            <result name="input">/indexURL.jsp</result>
        </action>

        <!-- 'giveAdminPrivilege' action calls 'execute' in 'giveAdminPrivilegeAction' -->
        <action name="giveAdminPrivilege" class="ucBusca.action.giveAdminPrivilegeAction" method="execute">
            <result name="success">/menu.jsp</result>
            <result name="input">/giveAdminPrivilege.jsp</result>
            <result name="error">/giveAdminPrivilege.jsp</result>
            <result name="none">/giveAdminPrivilege.jsp</result>
        </action>

        <!-- 'logout' action calls 'execute' in 'giveAdminPrivilegeAction' -->
        <action name="logout" class="ucBusca.action.LogoutAction" method="execute">
            <result name="success">/menu.jsp</result>
        </action>

        <!-- 'logout' action calls 'execute' in 'giveAdminPrivilegeAction' -->
        <action name="search" class="ucBusca.action.SearchAction" method="execute">
            <result name="success">/search.jsp</result>
        </action>

    </package>

</struts>