import {HttpClient} from '@angular/common/http';
import {Observable, of, switchMap} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthService} from '@app/gen-components/org/maiaframework/showcase/auth/auth.service';


export function appInitializer(authService: AuthService, http: HttpClient): () => Observable<any> {
    return () => http.get('/csrf').pipe(
        switchMap(() => authService.refreshToken()),
        catchError(() => of(null))
    );
}
