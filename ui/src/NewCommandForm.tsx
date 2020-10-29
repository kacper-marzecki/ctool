import { AutoComplete, Button, Form, Input, Select, Table, Tabs } from "antd";
import React, { ChangeEvent, useEffect, useState } from "react";
import { debounce } from "ts-debounce";
import { apiGet, apiPost, formTouchedAndValid, notEmpty, notifyError, prettyPrint, stateUpdateFn, wrapInField } from "./utils";
import { PlayCircleOutlined, SaveOutlined } from '@ant-design/icons';
import Modal from "antd/lib/modal/Modal";
import { StoredCommand } from "./model";
import { ColumnsType } from "antd/lib/table";
import { StoredCommandList } from "./StoredCommandList";
import { RecentCommandList } from "./RecentCommandList";

interface State {
    commands: { value: string }[],
    dirs: { value: string }[],
    options: string[],
    newCommandName: string,
    saveCommandOpen: boolean
}

export function NewCommandForm() {
    const [form] = Form.useForm<{ command: string, options: string[], dir: string }>()
    const [state, setState] = useState<State>({
        commands: [],
        dirs: [],
        options: [],
        newCommandName: "",
        saveCommandOpen: false
    })
    const updateStateAt = stateUpdateFn(setState)

    const saveStoredCommand = () => {
        const data = { ...form.getFieldsValue(), name: state.newCommandName }
        console.log(JSON.stringify(data))
        apiPost("command/stored", data)
            .catch(notifyError)
    }

    useEffect(() => {
        apiGet<string[]>("command/top-commands")
            .then(it => it.map(wrapInField("value")))
            .then(updateStateAt("commands"))
            .catch(notifyError)
        apiGet<string[]>("command/top-directories")
            .then(it => it.map(wrapInField("value")))
            .then(updateStateAt("dirs"))
            .catch(notifyError)
    }, [])

    const updateTopArgs = debounce((command: string) => {
        if (command) {
            apiGet<string[]>(`command/top-args/${command}`)
                .then(updateStateAt("options"))
                .catch(notifyError)
        }
    },
        1000);


    const changeCommand = (command: string) => {
        form.setFieldsValue({ command: command, options: [] })
        updateTopArgs(command);
    }

    const updateNewCommandName = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        updateStateAt("newCommandName")(value)
    }

    const setFormFromStoredCommand = (command: StoredCommand) => {
        form.setFieldsValue({ command: command.commandString, options: command.args, dir: command.dir })
        updateTopArgs(command.commandString);
    }

    return <div>
        <Form layout="inline" name="command-form" form={form}>
            <Form.Item name="command" label="Command" rules={[{ required: true, validator: notEmpty }]}>
                <AutoComplete style={{ width: 200 }}
                    options={state.commands}
                    value={form.getFieldValue("command")}
                    onChange={changeCommand}
                    placeholder="Enter command" />
            </Form.Item>
            <Form.Item name="dir" label="Directory">
                <AutoComplete style={{ minWidth: '20em' }}
                    options={state.dirs}
                    value={form.getFieldValue("dir")}
                    onChange={(it) => form.setFieldsValue({ dir: it })}
                    placeholder="input placeholder" />
            </Form.Item>
            <Form.Item name="options" label="Options">
                <Select mode="tags" style={{ minWidth: '20em' }} placeholder="Options">
                    {state.options.map(option => <Select.Option key={option} value={option}>{option}</Select.Option>)}
                </Select>
            </Form.Item>
            <Form.Item shouldUpdate>
                {() => <Button
                    icon={<SaveOutlined />}
                    disabled={formTouchedAndValid(form)}
                    onClick={() => updateStateAt("saveCommandOpen")(true)}
                >
                    Save
          </Button>}
            </Form.Item>
            <Form.Item shouldUpdate>
                {() => <Button
                    icon={<PlayCircleOutlined />}
                    disabled={formTouchedAndValid(form)}
                    onClick={() => console.error("NOT IMPLEMENTED")}
                >
                    Execute
          </Button>}
            </Form.Item>
        </Form>

        <Tabs defaultActiveKey="1" >
            <Tabs.TabPane tab="Saved" key="1">
                <StoredCommandList
                    selectCommand={setFormFromStoredCommand}
                    executeCommand={(command) => {
                        apiPost(`command/stored/${command.rowId}/execute`, undefined)
                        notifyError(`command Executed: ${prettyPrint(command)}`)
                    }} />
            </Tabs.TabPane>
            <Tabs.TabPane tab="Recent" key="2">
                <RecentCommandList
                    selectCommand={setFormFromStoredCommand}
                    executeCommand={(command) => notifyError(`command Executed: ${prettyPrint(command)}`)} />
            </Tabs.TabPane>
        </Tabs>

        <Modal
            title="Save Command"
            visible={state.saveCommandOpen}
            onOk={() => { saveStoredCommand(); updateStateAt("saveCommandOpen")(false) }}
            onCancel={() => updateStateAt("saveCommandOpen")(false)}
            okText="Save"
            cancelText="Cancel"
        >
            <Form.Item label="Command Name ">
                <Input value={state.newCommandName} onChange={updateNewCommandName} />
            </Form.Item>

        </Modal>
    </div >
}