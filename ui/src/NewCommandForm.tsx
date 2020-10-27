import { AutoComplete, Button, Form, Input, notification, Select } from "antd";
import { formatTimeStr } from "antd/lib/statistic/utils";
import Axios from "axios";
import { utimes } from "fs";
import React, { ChangeEvent, ChangeEventHandler, useEffect, useState } from "react";
import { debounce } from "ts-debounce";
import { match } from "ts-pattern";
import { getTokenSourceMapRange } from "typescript";
import { apiGet, apiPost, ApiResponse, formTouchedAndValid, notEmpty, notifyError, wrapInField } from "./utils";
import {
    SaveOutlined,
    PlayCircleOutlined
} from '@ant-design/icons';
import { RuleObject } from "antd/lib/form";
import { StoreValue } from "antd/lib/form/interface";
import { stringify } from "querystring";
import Modal from "antd/lib/modal/Modal";
import { NotificationInstance } from "antd/lib/notification";

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

    const saveStoredCommand = () => {
        const data = { ...form.getFieldsValue(), name: state.newCommandName }
        console.log(JSON.stringify(data))
        apiPost("command", data)
            .catch(notifyError)
    }

    useEffect(() => {
        apiGet<string[]>("command/top-commands")
            .then(it => it.map(wrapInField("value")))
            .then(it => setState(s => ({ ...s, commands: it })))
            .catch(notifyError)
        apiGet<string[]>("command/top-directories")
            .then(it => it.map(wrapInField("value")))
            .then(it => setState(s => ({ ...s, dirs: it })))
            .catch(notifyError)
    }, [])

    const updateTopArgs = debounce((command: string) => {
        if (command) {
            apiGet<string[]>(`command/top-args/${command}`)
                .then(it => setState(s => ({ ...s, options: it })))
                .catch(notifyError)
        }
        ;
    },
        1000);


    const changeCommand = (command: string) => {
        form.setFieldsValue({ command: command, options: [] })
        updateTopArgs(command);
    }

    const updateNewCommandName = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setState(s => ({ ...s, newCommandName: value }))
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
                <AutoComplete style={{ width: 200 }}
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
                    onClick={() => setState(s => ({ ...s, saveCommandOpen: true }))}
                >
                    Save
          </Button>}
            </Form.Item>
            <Form.Item shouldUpdate>
                {() => <Button
                    icon={<PlayCircleOutlined />}
                    disabled={formTouchedAndValid(form)}
                    onClick={() => console.error(form.getFieldsError())}
                >
                    Execute
          </Button>}
            </Form.Item>
        </Form>
        <Modal
            title="Save Command"
            visible={state.saveCommandOpen}
            onOk={() => { saveStoredCommand(); setState(s => ({ ...s, saveCommandOpen: false })) }}
            onCancel={() => setState(s => ({ ...s, saveCommandOpen: false }))}
            okText="Save"
            cancelText="Cancel"
        >
            <Form.Item label="Comand Name ">
                <Input value={state.newCommandName} onChange={updateNewCommandName} />
            </Form.Item>

        </Modal>
    </div >
}