import axios, { AxiosResponse } from "axios";
import { Payload } from "./models/Payload";

export interface ResponseResults {
	type: string;
	payload: any;
}
export class AJAX {
	static post = (url: string, body: any): Promise<Payload> => new Promise(async (success, failure) => {
		try {
			const response: AxiosResponse = await axios({
				method: "post",
				url: url,
				data: JSON.stringify(body),
				headers: {"content-type": "application/json"}
			});
			const responseJSON = await response.data;
			success(new Payload(responseJSON));
		} catch (error) {
			failure(error);
		}
	});

	static patch = (url: string, body: any): Promise<Payload> => new Promise(async (success, failure) => {
		try {
			const response: AxiosResponse = await axios({
				method: "patch",
				url: url,
				data: JSON.stringify(body),
				headers: {"content-type": "application/json"}
			});
			const responseJSON = await response.data;
			success(new Payload(responseJSON));
		} catch (error) {
			failure(error);
		}
	});
	
	
	static get = (url: string): Promise<Payload> => new Promise(async (success, failure) => {
		try {
			const response: AxiosResponse = await axios({
				method: "get",
				url: url,
				headers: {"content-type": "application/json"}
			});
			const responseJSON = await response.data;
			success(new Payload(responseJSON));
		} catch (error) {
			failure(error);
		}
	});


	static delete = (url: string, body: any): Promise<Payload> => new Promise(async (success, failure) => {
		try {
			const response: AxiosResponse = await axios({
				method: "delete",
				url: url,
				data: JSON.stringify(body),
				headers: {"content-type": "application/json"}
			});
			const responseJSON = await response.data;
			success(new Payload(responseJSON));
		} catch (error) {
			failure(error);
		}
	});
}