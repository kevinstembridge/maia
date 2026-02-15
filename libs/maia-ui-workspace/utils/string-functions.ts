
export function trimToNull(input: string): string {

  if (input === undefined || input == null || input.trim() === '') {
    return null;
  }

  return input.trim();

}
