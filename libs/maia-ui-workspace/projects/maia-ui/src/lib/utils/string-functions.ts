
export function trimToNull(input: string): string | null {

  if (input === undefined || input == null || input.trim() === '') {
    return null;
  }

  return input.trim();

}
