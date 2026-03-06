import {Observable, of, tap} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth/auth.service';


export function appInitializer(authService: AuthService): () => Observable<any> {
  return () => authService.refreshToken()
    .pipe(
      catchError(err => of(null))
    );
}
