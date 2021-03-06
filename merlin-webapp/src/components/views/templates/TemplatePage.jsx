import React from 'react';
import {Alert, Nav, NavLink, TabContent, Table, TabPane} from 'reactstrap';
import {formatDateTime, getRestServiceUrl} from '../../../utilities/global';
import classNames from 'classnames';
import {PageHeader} from '../../general/BootstrapComponents';
import LinkFile from '../../general/LinkFile';
import TemplateDefinitionPage from './templatedefinition/TemplateDefinitionPage';
import TemplateRunTab from './TemplateRunTab';
import TemplateSerialRunTab from './TemplateSerialRunTab';
import TemplateStatistics from './TemplateStatistics';
import LogEmbeddedPanel from '../logging/LogEmbeddedPanel';
import I18n from "../../general/translation/I18n";

class TemplatePage extends React.Component {

    state = {
        primaryKey: this.props.match.params.primaryKey,
        activeTab: '1',
        loading: true
    };

    componentDidMount = () => {
        fetch(getRestServiceUrl('templates/template', {
            primaryKey: this.state.primaryKey
        }), {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => {
                return response.json();
            })
            .then((json) => this.setState({
                loading: false,
                template: json
            }))
            .catch(() => this.setState({
                loading: false,
                error: true
            }));
    };

    toggleTab = tab => () => {

        if (tab === '5') {
            this.logComponent.current.reload();
        }

        this.setState({
        activeTab: tab
        })
    };

    render = () => {

        if (this.state.loading) {
            return <Alert color={'secondary'}>
                Loading Template {this.state.primaryKey}
            </Alert>;
        }

        if (this.state.error) {
            return <Alert color={'danger'}>
                No template found
            </Alert>
        }
        const template = this.state.template;
        let templateId = template.id ? template.id : template.fileDescriptor.filename;
        return (
            <React.Fragment>
                <PageHeader>
                    {templateId}
                </PageHeader>
                <Nav tabs>
                    <NavLink
                        className={classNames({active: this.state.activeTab === '1'})}
                        onClick={this.toggleTab('1')}
                    >
                        <I18n name={'common.common'}/>
                    </NavLink>
                    {this.state.template.templateDefinition ?
                        <NavLink
                            className={classNames({active: this.state.activeTab === '2'})}
                            onClick={this.toggleTab('2')}
                        >
                            <I18n name={'template.definition'}/>
                        </NavLink> : undefined}
                    <NavLink
                        className={classNames({active: this.state.activeTab === '3'})}
                        onClick={this.toggleTab('3')}
                    >
                        <I18n name={'common.run'}/>
                    </NavLink>
                    <NavLink
                        className={classNames({active: this.state.activeTab === '4'})}
                        onClick={this.toggleTab('4')}
                    >
                        <I18n name={'templates.serialRun'}/>
                    </NavLink>
                    <NavLink
                        className={classNames({active: this.state.activeTab === '5'})}
                        onClick={this.toggleTab('5')}
                    >
                        <I18n name={'logviewer'}/>
                    </NavLink>
                </Nav>
                <TabContent activeTab={this.state.activeTab}>
                    <TabPane tabId={'1'}>
                        <Table hover>
                            <tbody>
                            <tr>
                                <td><I18n name={'common.filename'}/></td>
                                <td>{this.state.template.fileDescriptor.filename}</td>
                            </tr>
                            <tr>
                                <td><I18n name={'common.lastModified'}/></td>
                                <td>{formatDateTime(this.state.template.fileDescriptor.lastModified)}</td>
                            </tr>
                            <tr>
                                <td><I18n name={'common.path'}/></td>
                                <td><LinkFile primaryKey={template.fileDescriptor.primaryKey}
                                              filepath={template.fileDescriptor.canonicalPath}/></td>
                            </tr>
                            <tr>
                                <td><I18n name={'templates.primaryKey'}/></td>
                                <td>{this.state.template.primaryKey}</td>
                            </tr>
                            </tbody>
                        </Table>
                        <TemplateStatistics statistics={template.statistics}/>
                    </TabPane>
                    {this.state.template.templateDefinition ?
                        <TabPane tabId={'2'}>
                            <div className="card border-secondary mb-3">
                                <div className="card-body">
                                    <TemplateDefinitionPage hidePageHeader={'true'}
                                                            match={{
                                                                params: {
                                                                    primaryKey: this.state.template.templateDefinition.fileDescriptor.primaryKey
                                                                }
                                                            }}
                                    />
                                </div>
                            </div>
                        </TabPane> : undefined}
                    <TabPane tabId={'3'}>
                        <TemplateRunTab
                            primaryKey={this.state.primaryKey}
                            templateDefinitionId={this.state.template.templateDefinition ?
                                this.state.template.templateDefinition.primaryKey : ''}
                            inputVariables={this.state.template.statistics.inputVariables}
                        />
                    </TabPane>
                    <TabPane tabId={'4'}>
                        <TemplateSerialRunTab
                            templatePrimaryKey={this.state.primaryKey}
                            templateDefinitionPrimaryKey={this.state.template.templateDefinition ?
                                this.state.template.templateDefinition.primaryKey : ''}
                        />
                    </TabPane>
                    <TabPane tabId={'5'}>
                        <LogEmbeddedPanel
                            ref={this.logComponent}
                            mdcTemplatePk={this.state.primaryKey}
                            mdcTemplateDefinitionPk={this.state.template.templateDefinition ?
                                this.state.template.templateDefinition.primaryKey : null}
                        />
                    </TabPane>
                </TabContent>
            </React.Fragment>
        );
    };

    constructor(props) {
        super(props);

        this.logComponent = React.createRef();

        this.toggleTab = this.toggleTab.bind(this);
    }
}


export default TemplatePage;
