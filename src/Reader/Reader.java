package Reader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Reader {
    private char separator = ';';
    private char quote = '"';
    private InputStreamReader reader;
    private List<String> line;
    private int lineCount = 1;

    public Reader(String filename) {
        try {
            this.reader = new InputStreamReader(getClass().getResourceAsStream(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    public void setQuote(char quote) {
        this.quote = quote;
    }

    public void skipRow() {
        this.loadRow();
        this.line = null;
    }

    public boolean loadRow() {
        if (this.reader == null) {
            this.line = null;
            return false;
        } else {
            this.line = new ArrayList();
            StringBuffer curVal = new StringBuffer();
            int charCount = 0;
            byte state = 0;

            try {
                while(true) {
                    int ch = this.reader.read();
                    ++charCount;
                    if (ch == 13) {
                        ch = this.reader.read();
                        ++charCount;
                        if (ch != 10) {
                            throw new Error("\\n expected after \\r at " + this.lineCount + ":" + charCount);
                        }
                    }

                    if (ch == this.quote) {
                        if (state == 0) {
                            state = 2;
                        } else if (state == 1) {
                            curVal.append((char)ch);
                        } else if (state == 2) {
                            state = 3;
                        } else if (state == 3) {
                            curVal.append(ch);
                            state = 2;
                        }
                    } else if (ch == this.separator) {
                        if (state == 2) {
                            curVal.append((char)ch);
                        } else {
                            this.line.add(curVal.toString());
                            curVal = new StringBuffer();
                            state = 0;
                        }
                    } else if (ch == 10) {
                        ++this.lineCount;
                        if (state != 2) {
                            this.line.add(curVal.toString());
                            return true;
                        }

                        charCount = 0;
                        curVal.append((char)ch);
                    } else {
                        if (ch == -1) {
                            this.reader = null;
                            if (state == 0 && this.line.isEmpty()) {
                                this.line = null;
                                return false;
                            }

                            if (state == 2) {
                                throw new Error("EOF within quoted value at " + this.lineCount + ":" + charCount);
                            }

                            this.line.add(curVal.toString());
                            return true;
                        }

                        if (state == 3) {
                            throw new Error("Expecting a quote, separator or newline after a closing quote at " + this.lineCount + ":" + charCount);
                        }

                        if (state == 0) {
                            state = 1;
                        }

                        curVal.append((char)ch);
                    }
                }
            } catch (IOException var5) {
                throw new Error("I/O error: " + var5);
            }
        }
    }
    
    public boolean isEmpty(int column) {
        String value = this.getString(column);
        return value.isEmpty();
    }

    public String getString(int column) {
        if (this.line == null) {
            if (this.reader == null) {
                throw new Error("Cannot call a get..() method after readLine() returned false");
            } else {
                throw new Error("Cannot call a get..() method before calling readLine()");
            }
        } else {
            return (String)this.line.get(column);
        }
    }

    public int getInt(int column) {
        String str = this.getString(column);

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException var4) {
            throw new Error("Value '" + str + "' cannot be converted to integer at line " + this.lineCount + " column " + column);
        }
    }

    public double getDouble(int column) {
        return Double.parseDouble(this.getString(column));
    }
}