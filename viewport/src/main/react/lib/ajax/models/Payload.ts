import { Body } from "./Body";

export interface ResponseError {
	code: string;
	message: string;
}
export class Payload {
	readonly statusCode: number;
	readonly error?: ResponseError;
	readonly data?: Body;

	constructor(statusCode: number, error: ResponseError, data: any) {
        this.statusCode = statusCode;
        if(error != null) this.error = error as ResponseError;
        if(data != null) this.data = data;

	}

    get success() {
        return this.statusCode == 200;
    }
	
	spreadResponse = (): [ ResponseError?, Body? ] => [ this.error, this.data ];
}