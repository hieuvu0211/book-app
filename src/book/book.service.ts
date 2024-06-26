import { Injectable, Res } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import * as path from 'path';
import * as fs from 'fs';
import { Response } from 'express';
@Injectable()
export class BookService {
  constructor(private prisma: PrismaService) {}

  async getAllBooks() {
    try {
      const res = await this.prisma.book.findMany();
      if (res.length > 0 || res) {
        return res;
      } else return { status: 400, message: 'No book found' };
    } catch (error) {
      throw new Error(error);
    }
  }

  async getBookById(id: string) {
    
    
    try {
      const res = await this.prisma.book.findUnique({
        where: {
          book_id: Number(id),
        },
      });
      if (res) {
        return res;
      } else {
        console.log("error ");
        return { status: 400, message: 'No book found' };
      }
    } catch (error) {
      console.log("error = ",error);
      throw new Error(error);
    }
  }

  async getImage(
    ImgPath: String,
    chpater: String,
    page: String,
    @Res() res: Response,
  ) {
    try {
      const imagePath = path.join(
        __dirname,
        '..',
        `../public/Books/${ImgPath}/${chpater}/`,
        `${page}.jpg`,
      );
      const imageExists = fs.existsSync(imagePath);
      console.log(imagePath, '  ', imageExists);
      if (!imageExists) {
        // If image doesn't exist, return 404
        return res.sendStatus(404);
      }
      fs.createReadStream(imagePath).pipe(res);
    } catch (error) {
      return res.sendStatus(500);
    }
  }

  //get avatar book by name
  async getImageBookByName(idBook: string, @Res() res: Response) {
    try {
      const imagePath = path.join(
        __dirname,
        '..',
        `../public/Books/${idBook}/`,
        `image.png`,
      );

      let imageLinks = `http://localhost:8080/Books/${idBook}/image.png`;

      const folderPath = path.join(
        __dirname,
        '..',
        `../public/Books/`,
        `${idBook}`,
      );
      let folderCount: number = 0;
      const items = fs.readdirSync(folderPath);

      folderCount = items.length - 1;

      return res.json({ folderCount, imageLinks });
    } catch (error) {
      return res.sendStatus(500);
    }
  }

  //get number of chapter
  async getApiNumberChapter(name: string,chapter: string, @Res() res: Response) {
    try {
      const folderPath = path.join(
        __dirname,
        '..',
        `../public/Books/${name}/`,
        `${chapter}`,
      );
      let folderCount: number = 0;
      const items = fs.readdirSync(folderPath);

      folderCount = items.length ;
      let ListApiImage:Array<string> = [];
      for(let i = 1; i <= folderCount; i++){
        if(i < 10) {
          ListApiImage.push(`http://10.0.2.2:8080/Books/${name}/${chapter}/0${i}.jpg`)
          continue;
        }
        else ListApiImage.push(`http://10.0.2.2:8080/Books/${name}/${chapter}/${i}.jpg`)
      }
      return res.json({ ListApiImage });
    } catch (error) {
      console.log("error = ",error);
      return res.sendStatus(500);
    }
  }

  async SearchBookByName(name: string) {
    try {
      const res = await this.prisma.book.findMany({
        where: {
          book_name: {
            contains: name,
          },
        },
      });
      if (res.length > 0 || res) {
        return res;
      } else return { status: 400, message: 'No book found' };
    } catch (error) {
      throw new Error(error);
    }
  }

  async GetTopTenBookByNumberOfLike() {
    try {
      const res = await this.prisma.book.findMany({
        orderBy: {
          number_of_likes: 'desc',
        },
        take: 10,
      });
      if (res.length > 0 || res) {
        return res;
      } else return { status: 400, message: 'No book found' };
    } catch (error) {
      throw new Error(error);
    }
  }
}
