import { AutoComplete, Button, Form, Input, Select } from "antd";
import { formatTimeStr } from "antd/lib/statistic/utils";
import Axios from "axios";
import { utimes } from "fs";
import React, { useEffect, useState } from "react";
import { debounce } from "ts-debounce";
import { match } from "ts-pattern";
import { getTokenSourceMapRange } from "typescript";
import { apiGet, ApiResponse, formTouchedAndValid, notEmpty, wrapInField } from "./utils";
import {
    SaveOutlined,
    PlayCircleOutlined
} from '@ant-design/icons';
import { RuleObject } from "antd/lib/form";
import { StoreValue } from "antd/lib/form/interface";
import { stringify } from "querystring";

interface State {
    commands: { value: string }[],
    dirs: { value: string }[],
    options: string[],
    newCommandName: string
}

export function NewCommandForm() {
    const [form] = Form.useForm<{ command: string, options: string[], dir: string }>()
    const [state, setState] = useState<State>({
        commands: [],
        dirs: [],
        options: [],
        newCommandName: ""
    })

    const saveStoredCommand = () => {
        const data = { ...form.getFieldsValue(), name: "test" }
        Axios.post("http://localhost:8080/api/command", data)
    }

    useEffect(() => {
        apiGet<string[]>("command/top-commands")
            .then(it => it.map(wrapInField("value")))
            .then(it => setState(s => ({ ...s, commands: it })));
        apiGet<string[]>("command/top-directories")
            .then(it => it.map(wrapInField("value")))
            .then(it => setState(s => ({ ...s, dirs: it })))
    }, [])

    const updateTopArgs = debounce((command: string) => {
        if (command) {
            apiGet<string[]>(`command/top-args/${command}`)
                .then(it => setState(s => ({ ...s, options: it })))
        }
        ;
    },
        1000);

    const changeCommand = (command: string) => {
        form.setFieldsValue({ command: command, options: [] })
        updateTopArgs(command);
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
                    onClick={saveStoredCommand}
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
    </div >
}