export interface ErrorDetails {
  summary: string;
  context?: Map<string, string>;
}

export class HttpErrorUtils {

  static errorDetailsFromHttpError(error: any): ErrorDetails {

    if (error.error instanceof ErrorEvent) {

      console.error('An unexpected error occurred', error.error.message);

      return {
        summary: 'Unexpected error: ' + error.error.message
      };

    } else {

      console.error('Unsuccessful HTTP response', error);

      const errorDetails: ErrorDetails = {
        summary: `Unsuccessful HTTP response (${error.statusText})`,
        context: new Map()
      };

      if (error.statusText) {
        const statusStr = error.status ? error.statusText + ' ()' + error.status + ')' : error.statusText;
        errorDetails.context!.set('status', statusStr);
      }

      if (error.url) {
        errorDetails.context!.set('url', error.url);
      }

      if (error.error) {

        if (error.error.timestamp) {
          errorDetails.context!.set('timestamp', error.error.timestamp);
        }

        if (error.error.message) {
          errorDetails.context!.set('message', error.error.message);
        }

      }

      return errorDetails;

    }

  }

}
