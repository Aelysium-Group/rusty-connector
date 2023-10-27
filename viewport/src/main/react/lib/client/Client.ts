export class Client {
    static uuid: string;
    static username: string;
    static permissions: string[];

    static hasPermission = (permissions: string[]): boolean => {
        if(this.permissions.includes('*')) return true;

        for (let index = 0; index < permissions.length; index++) {
            const permission = permissions[index];
            
            if(this.permissions.includes(permission)) return true;
    
            const parsedPermission = permission.replace(new RegExp('[A-z]*$','gi'),'*');
    
            return this.permissions.includes(parsedPermission);
        }

        return false;
    }
}