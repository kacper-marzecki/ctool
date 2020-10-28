import React, { ReactElement, useEffect, useState } from 'react';
import './App.css';
import { faTerminal } from '@fortawesome/free-solid-svg-icons';
import { AppViewPort } from './AppViewPort';
import { CommandPage } from './CommandPage';
import { useDispatch, useSelector, useStore } from "react-redux";
import { AppDispatch, useApp, useAppState } from "./store";

import { Button } from "antd";
import { AppState, changePage, decrement, increment, RootState } from "./rootReducer";
import { Layout, Menu, Breadcrumb } from 'antd';
import {
    RocketOutlined,
    TeamOutlined,
    CalendarOutlined,
} from '@ant-design/icons';
import { CommandsView } from "./CommandsView";
import { match } from 'ts-pattern';
import {stateUpdateFn} from "./utils";

const { Header, Content, Footer, Sider } = Layout;
const { SubMenu } = Menu;

function App() {
    const [appState, dispatch] = useApp();
    const [state, setState] = useState({
        collapsed: true,
    });
    const updateStateAt = stateUpdateFn(setState)

    const content = match(appState.selectedPage)
        .with("command", _ => <CommandsView />)
        .with("calendar", _ => /* TODO */<h1>"Not implemented"</h1>)
        .run()

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider collapsible collapsed={state.collapsed} onCollapse={updateStateAt("collapsed")}>
                <Menu theme="dark" defaultSelectedKeys={['1']} mode="inline">
                    <Menu.Item key="1" onClick={(e) => dispatch(changePage("command"))} icon={<RocketOutlined />}>
                        Run Command
                    </Menu.Item>
                    <Menu.Item key="2" onClick={(e) => dispatch(changePage("calendar"))} icon={<CalendarOutlined />}>
                        Calendar
                    </Menu.Item>
                    <SubMenu key="sub3" icon={<TeamOutlined />} title="Team">
                        <Menu.Item key="4">Stub</Menu.Item>
                        <Menu.Item key="5">Stub</Menu.Item>
                    </SubMenu>
                </Menu>
            </Sider>
            <Layout>
                <Header style={{ padding: 0 }} />
                <Content style={{ margin: '0 16px' }}>
                    <div style={{ padding: 20, minHeight: 360 }}>
                        {content}
                    </div>
                </Content>
            </Layout>
        </Layout>
    );
}

export default App;
