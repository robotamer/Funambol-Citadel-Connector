delete from fnbl_module where id='bmCitadel';
insert into fnbl_module (id, name, description)
values('bmCitadel','bmCitadel','Citadel Module');

delete from fnbl_connector where id='citadel';
insert into fnbl_connector(id, name, description)
values('citadel','citadel','Citadel Connector');

--
-- SyncSource Types
--
delete from fnbl_sync_source_type where id='citadelMail';
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('citadelMail','Citadel Mail Sync','net.bionicmessage.funambol.citadel.sync.CitadelSyncSource',
'net.bionicmessage.funambol.citadel.admin.CitadelSourceAdmin');


--
-- Connector source types
--
delete from fnbl_connector_source_type where connector='citadel' and sourcetype='citadelMail';
insert into fnbl_connector_source_type(connector, sourcetype)
values('citadel','citadelMail');

--
-- Module - Connector
--
delete from fnbl_module_connector where module='bmCitadel' and connector='citadel';
insert into fnbl_module_connector(module, connector)
values('bmCitadel','citadel');
