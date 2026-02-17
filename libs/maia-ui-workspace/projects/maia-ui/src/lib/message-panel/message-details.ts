import { ErrorDetails, HttpErrorUtils } from '../utils/HttpErrorUtils';

export class MessageDetails {

  errorDetails?: ErrorDetails = undefined;

  setErrorDetailsFromHttpError(error: any) {
    this.errorDetails = HttpErrorUtils.errorDetailsFromHttpError(error);
  }

  setErrorMessage(message: string) {
    this.errorDetails = { summary: message };
  }

}
