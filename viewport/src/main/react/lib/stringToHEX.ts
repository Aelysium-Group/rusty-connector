export const stringToHex = (string: string) => {
    let hex = '';
    for (let i = 0; i < string.length; i++) {
      const charCode = string.charCodeAt(i);
      const hexValue = charCode.toString(16);
  
      // Pad with zeros to ensure two-digit representation
      hex += hexValue.padStart(2, '0');
    }
    return hex;
  };