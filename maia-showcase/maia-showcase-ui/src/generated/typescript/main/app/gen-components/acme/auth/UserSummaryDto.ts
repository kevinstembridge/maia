import {Authority} from '@app/gen-components/acme/auth/Authority';

export interface UserSummaryDto {
    firstName: string;
    grantedAuthorities: Authority[];
    id: string;
    lastName: string;
}
