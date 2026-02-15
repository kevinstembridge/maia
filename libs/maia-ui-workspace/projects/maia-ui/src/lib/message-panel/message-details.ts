import { ErrorDetails, HttpErrorUtils } from '@app/utils/HttpErrorUtils';

export class MessageDetails {

  errorDetails: ErrorDetails = null;

  setErrorDetailsFromHttpError(error: any) {
    this.errorDetails = HttpErrorUtils.errorDetailsFromHttpError(error);
  }

  setErrorMessage(message: string) {
    this.errorDetails = { summary: message };
  }

}
