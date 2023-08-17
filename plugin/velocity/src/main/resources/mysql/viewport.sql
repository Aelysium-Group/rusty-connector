CREATE TABLE IF NOT EXISTS users (
    uuid VARCHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(36) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL,
    password  VARCHAR(256) NOT NULL,
    mfa_key VARCHAR(32) NOT NULL,
    locked TINYINT unsigned default 0,
);

CREATE TABLE IF NOT EXISTS users_roles_fk (
    user_uuid VARCHAR(36) NOT NULL,
    role_name VARCHAR(32) NOT NULL,
    CONSTRAINT uc_ids UNIQUE (user_id, role_id),
    CONSTRAINT users_fk FOREIGN KEY (user_uuid) REFERENCES users (uuid) ON DELETE CASCADE,
    CONSTRAINT roles_fk FOREIGN KEY (role_name) REFERENCES roles (name) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS roles (
    name VARCHAR(32) NOT NULL PRIMARY KEY,
    description VARCHAR(128) NOT NULL,
    color VARCHAR(6) NOT NULL,
);

CREATE TABLE IF NOT EXISTS roles_permissions_fk (
    role_name VARCHAR(32) NOT NULL,
    permission_identifier VARCHAR(32) NOT NULL,
    CONSTRAINT uc_ids UNIQUE (role_id, permission_id),
    CONSTRAINT roles_fk FOREIGN KEY (role_name) REFERENCES roles (name) ON DELETE CASCADE,
    CONSTRAINT permissions_fk FOREIGN KEY (permission_identifier) REFERENCES permissions (identifier) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS permissions (
    identifier VARCHAR(32) PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    description VARCHAR(128) NOT NULL,
);

REPLACE INTO permissions VALUES
("users.create",                "Create User Accounts", "Allows users to create new accounts."),
("users.delete",                "Delete User Accounts", "Allows users to delete other accounts."),
("users.lock",                  "Lock User Accounts", "Allows users to lock other accounts."),
("users.credentials.password",  "Edit User Passwords", "Allows users to view and reset the passwords of other accounts."),
("users.credentials.username",  "Edit User Usernames", "Allows users to view and edit the username of other accounts."),
("users.credentials.email",     "Edit User Emails", "Allows users to view and edit the email of other accounts."),
("users.credentials.roles",     "Edit User Roles", "Allows users to view and edit the roles of other accounts."),

("self.credentials.password",   "Edit Own Password", "Allows users to reset their own passwords."),
("self.credentials.username",   "Edit Own Username", "Allows users to edit their own usernames."),
("self.credentials.email",      "Edit Own Email", "Allows users to edit their own emails."),
("", "", "");